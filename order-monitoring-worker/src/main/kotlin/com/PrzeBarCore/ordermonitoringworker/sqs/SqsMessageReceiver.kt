package com.PrzeBarCore.ordermonitoringworker.sqs

import com.PrzeBarCore.ordermonitoringworker.dto.PublishedEvent
import com.PrzeBarCore.ordermonitoringworker.routing.EventRouter
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import tools.jackson.databind.ObjectMapper

@Profile("aws")
@Component
class SqsMessageReceiver(
    private val objectMapper: ObjectMapper,
    private val sqsClient: SqsClient,
    private val eventRouter: EventRouter,
    @Value("\${aws.sqs.queue-url}") private val queueUrl: String) {
    private val log = LoggerFactory.getLogger(SqsMessageReceiver::class.java)

    @Scheduled(fixedDelay = 2000)
    fun receiveMessages(){
        val receiveMessageRequest = ReceiveMessageRequest.builder()
            .queueUrl(queueUrl)
            .maxNumberOfMessages(1)
            .waitTimeSeconds(20)
            .build()

        val receiveMessageResponse = sqsClient.receiveMessage(receiveMessageRequest)
        for (message in receiveMessageResponse.messages()){
            try{
                val event = objectMapper.readValue(message.body(), PublishedEvent::class.java)
                log.info("Received event id={}, type={}",
                    event.eventId,
                    event.eventType)

                eventRouter.route(event)

                val deleteRequest = DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(message.receiptHandle())
                    .build()

                sqsClient.deleteMessage(deleteRequest)
            } catch (exception: Exception){
                log.error(
                    "Error when processing SQS message id={}",
                    message.messageId(),
                    exception
                )
            }
        }
    }
}