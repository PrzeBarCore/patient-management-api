package com.przebarcore.ordermonitoringworker.outbox.sender

import com.przebarcore.ordermonitoringworker.dto.outgoing.PublishedEvent
import com.przebarcore.ordermonitoringworker.entity.OutboxEvent
import com.przebarcore.ordermonitoringworker.exception.EventSerializationException
import tools.jackson.core.JacksonException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import tools.jackson.databind.ObjectMapper

@Component
@Profile("aws")
class SqsEventSender(
    private val sqsClient: SqsClient,
    @Value("\${aws.sqs.notification-queue-url}")
    private val  queueUrl: String,
    private val objectMapper: ObjectMapper) : EventSender{

    companion object{
        private val log = LoggerFactory.getLogger(SqsEventSender::class.java)
    }

    override fun send(event: OutboxEvent) {
        try{
            val eventToPublish = PublishedEvent(
                event.id!!, event.eventType, event.aggregateType,
                event.aggregateId, event.createdAt, objectMapper.readTree(event.payload))

            val request =  SendMessageRequest.builder()
                .queueUrl(this.queueUrl)
                .messageBody(objectMapper.writeValueAsString(eventToPublish))
                .messageGroupId(event.aggregateType.name + ":" + event.aggregateId)
                .messageDeduplicationId(event.id.toString()).build()
            val response = this.sqsClient.sendMessage(request)
            log.info("SQS responded. Received message id: {}", response.messageId())
        } catch (exception: JacksonException){
            log.error("Error while serializing outbox event {}", event.id, exception)
            throw EventSerializationException(event.aggregateType, event.aggregateId, exception)
        }
    }
}
