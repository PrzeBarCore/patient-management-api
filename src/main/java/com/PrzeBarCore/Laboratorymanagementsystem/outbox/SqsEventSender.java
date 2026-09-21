package com.PrzeBarCore.Laboratorymanagementsystem.outbox;

import com.PrzeBarCore.Laboratorymanagementsystem.dto.events.PublishedEvent;
import com.PrzeBarCore.Laboratorymanagementsystem.entity.OutboxEvent;
import com.PrzeBarCore.Laboratorymanagementsystem.exception.EventSerializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
@Profile("aws")
public class SqsEventSender implements EventSender{
    private static final Logger log = LoggerFactory.getLogger(SqsEventSender.class);
    private final String queueUrl;
    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    SqsEventSender(SqsClient sqsClient, @Value("${aws.sqs.queue-url}") String queueUrl, ObjectMapper objectMapper) {
        this.sqsClient = sqsClient;
        this.queueUrl = queueUrl;
        this.objectMapper = objectMapper;
    }

    @Override
    public void send(OutboxEvent event) {
        try{
            var eventToPublish = new PublishedEvent(event.getId(), event.getEventType(), event.getAggregateType(),
                    event.getAggregateId(), event.getCreatedAt(), objectMapper.readTree(event.getPayload()));

            var request =  SendMessageRequest.builder()
                    .queueUrl(this.queueUrl)
                    .messageBody(objectMapper.writeValueAsString(eventToPublish))
                    .messageGroupId(event.getAggregateType().name() + ":" + event.getAggregateId())
                    .messageDeduplicationId(event.getId().toString()).build();
            var response = this.sqsClient.sendMessage(request);
            log.info("SQS responded. Received message id: {}", response.messageId());
        } catch (JacksonException exception){
            log.error("Error while serializing outbox event {}", event.getId(), exception);
            throw new EventSerializationException(event.getAggregateType(), event.getAggregateId(), exception);
        }


    }
}
