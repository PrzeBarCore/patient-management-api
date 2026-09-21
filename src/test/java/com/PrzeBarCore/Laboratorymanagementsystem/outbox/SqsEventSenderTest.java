package com.PrzeBarCore.Laboratorymanagementsystem.outbox;

import com.PrzeBarCore.Laboratorymanagementsystem.dto.events.MedicalOrderStatusChangedEvent;
import com.PrzeBarCore.Laboratorymanagementsystem.entity.OutboxEvent;
import com.PrzeBarCore.Laboratorymanagementsystem.global.AggregateType;
import com.PrzeBarCore.Laboratorymanagementsystem.global.EventType;
import com.PrzeBarCore.Laboratorymanagementsystem.global.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SqsEventSenderTest {

    private static final String QUEUE_URL = "testQueueUrl";

    @Mock
    private SqsClient sqsClient;
    private ObjectMapper objectMapper;
    private SqsEventSender sqsEventSender;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder()
                .findAndAddModules()
                .build();

        sqsEventSender = new SqsEventSender(
                sqsClient,
                QUEUE_URL,
                objectMapper
        );
    }

    @Test
    void shouldSendMessage() {
        // Arrange
        Long eventId = 1L;
        Long orderId = 1L;
        LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        var payload = new MedicalOrderStatusChangedEvent(
                orderId,
                OrderStatus.NEW,
                OrderStatus.IN_PROCESS,
                createdAt
        );

        var event = new OutboxEvent();
        event.setId(eventId);
        event.setEventType(EventType.MEDICAL_ORDER_STATUS_CHANGED);
        event.setAggregateType(AggregateType.MEDICAL_ORDER);
        event.setAggregateId(orderId);
        event.setCreatedAt(createdAt);
        event.setPayload(objectMapper.writeValueAsString(payload));

        when(sqsClient.sendMessage(any(SendMessageRequest.class))).thenReturn(SendMessageResponse.builder().messageId("stubMessageId").build());
        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        // Act
        sqsEventSender.send(event);

        // Assert
        verify(sqsClient).sendMessage(captor.capture());

        SendMessageRequest request = captor.getValue();
        assertThat(request.queueUrl()).isEqualTo(QUEUE_URL);
        assertThat(request.messageGroupId()).isEqualTo("MEDICAL_ORDER:" + orderId);
        assertThat(request.messageDeduplicationId()).isEqualTo(eventId.toString());

        var body = objectMapper.readTree(request.messageBody());
        assertThat(body.get("eventId").asLong()).isEqualTo(eventId);
        assertThat(body.get("eventType").asString()).isEqualTo(EventType.MEDICAL_ORDER_STATUS_CHANGED.name());
        assertThat(body.get("aggregateType").asString()).isEqualTo(AggregateType.MEDICAL_ORDER.name());
        assertThat(body.get("aggregateId").asLong()).isEqualTo(orderId);
        assertThat(body.get("createdAt").asString()).isEqualTo(createdAt.toString());
        assertThat(body.get("payload").isObject()).isTrue();
        assertThat(body.get("payload").get("orderId").asLong()).isEqualTo(orderId);
        assertThat(body.get("payload").get("oldStatus").asString()).isEqualTo(OrderStatus.NEW.name());
        assertThat(body.get("payload").get("newStatus").asString()).isEqualTo(OrderStatus.IN_PROCESS.name());
    }
}