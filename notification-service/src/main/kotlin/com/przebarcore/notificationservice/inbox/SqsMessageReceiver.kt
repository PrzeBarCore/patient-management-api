package com.przebarcore.notificationservice.inbox

import com.przebarcore.notificationservice.dto.incoming.RetrievedEvent
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest
import software.amazon.awssdk.services.sqs.model.MessageSystemAttributeName
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import tools.jackson.databind.ObjectMapper
import kotlin.jvm.java

@Profile("aws")
@Component
class SqsMessageReceiver(
    private val objectMapper: ObjectMapper,
    private val sqsClient: SqsClient,
    @Value("\${aws.sqs.notification-queue-url}")
    private val queueUrl: String,
    private val eventProcessor: EventProcessor
) {

    companion object{
        private val logger = LoggerFactory.getLogger(SqsMessageReceiver::class.java.name)
    }

    @Scheduled(fixedDelay = 5000)
    fun receiveMessage(){
        val receiveMessageRequest = ReceiveMessageRequest.builder()
            .queueUrl(queueUrl)
            .maxNumberOfMessages(1)
            .waitTimeSeconds(20)
            .messageSystemAttributeNames(MessageSystemAttributeName.APPROXIMATE_RECEIVE_COUNT)
            .build()

        val response = sqsClient.receiveMessage(receiveMessageRequest)

        for (message in response.messages()){
            val receiveCount =
                message.attributes()[MessageSystemAttributeName.APPROXIMATE_RECEIVE_COUNT]
                    ?: "unknown"
            try{
                val event = objectMapper.readValue(message.body(), RetrievedEvent::class.java)
                logger.info("Received event id={}, type={}, receive count={}",
                    event.eventId,
                    event.eventType,
                    receiveCount)

                eventProcessor.process(event)

                val deleteRequest = DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(message.receiptHandle())
                    .build()

                sqsClient.deleteMessage(deleteRequest)
            } catch (exception: Exception){
                logger.error(
                    "Error when processing SQS message id={}, retry count={}",
                    message.messageId(),
                    receiveCount,
                    exception
                )
            }
        }

    }
}