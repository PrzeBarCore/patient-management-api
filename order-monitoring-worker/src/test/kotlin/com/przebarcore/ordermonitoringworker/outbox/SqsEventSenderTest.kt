package com.przebarcore.ordermonitoringworker.outbox

import com.przebarcore.ordermonitoringworker.dto.outgoing.NotificationRequestedEvent
import com.przebarcore.ordermonitoringworker.entity.OutboxEvent
import com.przebarcore.ordermonitoringworker.exception.EventSerializationException
import com.przebarcore.ordermonitoringworker.global.enums.outgoing.NotificationType
import com.przebarcore.ordermonitoringworker.global.enums.outgoing.OutboxAggregateType
import com.przebarcore.ordermonitoringworker.global.enums.outgoing.OutboxEventType
import com.przebarcore.ordermonitoringworker.global.enums.outgoing.ReferenceType
import com.przebarcore.ordermonitoringworker.outbox.sender.SqsEventSender
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import software.amazon.awssdk.protocols.jsoncore.JsonNode
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import software.amazon.awssdk.services.sqs.model.SendMessageResponse
import tools.jackson.databind.ObjectMapper
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class SqsEventSenderTest {
    @Mock
    private lateinit var sqsClient: SqsClient

    private val objectMapper = ObjectMapper()

    private lateinit var sqsEventSender: SqsEventSender
    private val queueUrl = "queueUrl"

    @BeforeEach
    fun setUp(){
        sqsEventSender = SqsEventSender(sqsClient, queueUrl, objectMapper)
    }

    @Test
    fun shouldSendOutboxEventToNotificationQueue(){
        val notificationRequestedEventId = UUID.randomUUID()
        val referenceType = ReferenceType.MEDICAL_ORDER
        val orderId = 1L
        val notificationType = NotificationType.ESCALATION
        val now = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
        val createdAt = now.minusHours(1)

        val payload = objectMapper.writeValueAsString(
            NotificationRequestedEvent(notificationRequestedEventId,
                referenceType,
                orderId,
                notificationType,
                createdAt))

        val outboxEventId = 10L
        val aggregateType = OutboxAggregateType.MEDICAL_ORDER
        val eventType = OutboxEventType.NOTIFICATION_REQUESTED

        val outboxEvent = OutboxEvent(outboxEventId,
            orderId,
            aggregateType,
            eventType,
            payload,
            createdAt,
            null)

        whenever(sqsClient.sendMessage(any<SendMessageRequest>()))
            .thenReturn(SendMessageResponse.builder().messageId("messageId1").build())

        sqsEventSender.send(outboxEvent)

        val argumentCaptor = ArgumentCaptor.forClass(SendMessageRequest::class.java)
        verify(sqsClient).sendMessage(argumentCaptor.capture())

        val messageRequest = argumentCaptor.value
        assertThat(messageRequest.queueUrl()).isEqualTo(queueUrl)
        assertThat(messageRequest.messageGroupId()).isEqualTo(aggregateType.name + ":" + orderId)
        assertThat(messageRequest.messageDeduplicationId()).isEqualTo(outboxEventId.toString())

        val jsonObject = JsonNode.parser().parse(messageRequest.messageBody()).asObject()
        assertThat(jsonObject.getValue("eventId").asNumber()).isEqualTo(outboxEventId.toString())
        assertThat(jsonObject.getValue("eventType").asString()).isEqualTo(eventType.name)
        assertThat(jsonObject.getValue("aggregateType").asString()).isEqualTo(aggregateType.name)
        assertThat(jsonObject.getValue("aggregateId").asNumber()).isEqualTo(orderId.toString())
        assertThat(jsonObject.getValue("createdAt").asString()).isEqualTo(createdAt.toString())
        assertThat(jsonObject.getValue("payload").isObject).isTrue

        val payloadFromRequest = jsonObject.getValue("payload").asObject()
        assertThat(payloadFromRequest.getValue("requestId").asString()).isEqualTo(notificationRequestedEventId.toString())
        assertThat(payloadFromRequest.getValue("referenceType").asString()).isEqualTo(referenceType.name)
        assertThat(payloadFromRequest.getValue("referenceId").asNumber()).isEqualTo(orderId.toString())
        assertThat(payloadFromRequest.getValue("notificationType").asString()).isEqualTo(notificationType.name)
        assertThat(payloadFromRequest.getValue("createdAt").asString()).isEqualTo(createdAt.toString())
    }

    @Test
    fun shouldThrowEventSerializationExceptionWhenPayloadCannotBeDeserialized() {

        val orderId = 1L
        val now = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
        val createdAt = now.minusHours(1)
        val outboxEventId = 10L
        val aggregateType = OutboxAggregateType.MEDICAL_ORDER
        val eventType = OutboxEventType.NOTIFICATION_REQUESTED

        val outboxEvent = OutboxEvent(
            outboxEventId,
            orderId,
            aggregateType,
            eventType,
            "I don't feel too well mr Stark",
            createdAt,
            null
        )


        val exception = assertThrows<EventSerializationException> { sqsEventSender.send(outboxEvent) }

        assertThat(exception.message).contains("Serialization exception for: " + aggregateType.name + " of id " + orderId)
        verify(sqsClient, never()).sendMessage(any<SendMessageRequest>())
    }

    @Test
    fun shouldPropagateExceptionWhenSqsSendingFails(){
        val notificationRequestedEventId = UUID.randomUUID()
        val referenceType = ReferenceType.MEDICAL_ORDER
        val orderId = 1L
        val notificationType = NotificationType.ESCALATION
        val now = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
        val createdAt = now.minusHours(1)

        val payload = objectMapper.writeValueAsString(
            NotificationRequestedEvent(notificationRequestedEventId,
                referenceType,
                orderId,
                notificationType,
                createdAt))

        val outboxEventId = 10L
        val aggregateType = OutboxAggregateType.MEDICAL_ORDER
        val eventType = OutboxEventType.NOTIFICATION_REQUESTED

        val outboxEvent = OutboxEvent(outboxEventId,
            orderId,
            aggregateType,
            eventType,
            payload,
            createdAt,
            null)

        doThrow(RuntimeException("HMM, zbiera się na burze")).whenever(sqsClient).sendMessage(any<SendMessageRequest>())

        val exception = assertThrows<RuntimeException> { sqsEventSender.send(outboxEvent) }

        assertThat(exception.message).contains("HMM, zbiera się na burze")
        verify(sqsClient).sendMessage(any<SendMessageRequest>())
    }
}