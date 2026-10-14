package com.przebarcore.notificationservice.inbox


import com.przebarcore.notificationservice.dto.incoming.RetrievedEvent
import com.przebarcore.notificationservice.global.enums.InboxAggregateType
import com.przebarcore.notificationservice.global.enums.InboxEventType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest
import software.amazon.awssdk.services.sqs.model.Message
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.node.JsonNodeFactory
import tools.jackson.module.kotlin.KotlinModule
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class SqsMessageReceiverTest {
    private val objectMapper = JsonMapper
        .builder()
        .addModule(KotlinModule.Builder().build())
        .build()
    @Mock
    private lateinit var sqsClient: SqsClient
    @Mock
    private lateinit var eventProcessor: EventProcessor
    private lateinit var sqsMessageReceiver: SqsMessageReceiver

    @BeforeEach
    fun setUp() {
        sqsMessageReceiver = SqsMessageReceiver(objectMapper, sqsClient, "queueUrl", eventProcessor)
    }

    @Test
    fun shouldDeleteMessageAfterSuccessfulProcessing(){
        val eventId = 1L
        val eventType = InboxEventType.NOTIFICATION_REQUESTED
        val aggregateType = InboxAggregateType.MEDICAL_ORDER
        val aggregateId = 10L
        val createdAt = LocalDateTime.now()
        val payload = JsonNodeFactory.instance.objectNode()
        val retrievedEvent = RetrievedEvent(eventId, eventType, aggregateType, aggregateId, createdAt, payload)

        val response = ReceiveMessageResponse
            .builder().messages(
            Message.builder()
                .body(objectMapper.writeValueAsString(retrievedEvent))
                .receiptHandle("handle")
                .build())
            .build()

        whenever(sqsClient.receiveMessage(any<ReceiveMessageRequest>())).thenReturn(response)

        sqsMessageReceiver.receiveMessage()

        val captor= argumentCaptor<RetrievedEvent>()
        verify(eventProcessor).process(captor.capture())

        val deserializedObject = captor.firstValue
        assertThat(deserializedObject.aggregateId).isEqualTo(aggregateId)
        assertThat(deserializedObject.eventId).isEqualTo(eventId)
        assertThat(deserializedObject.eventType).isEqualTo(eventType)
        assertThat(deserializedObject.aggregateType).isEqualTo(aggregateType)
        assertThat(deserializedObject.createdAt).isEqualTo(createdAt)

        val captor2= argumentCaptor<DeleteMessageRequest>()
        verify(sqsClient).deleteMessage(captor2.capture())

        val deleteMessageRequest = captor2.firstValue
        assertThat(deleteMessageRequest.queueUrl()).isEqualTo("queueUrl")
        assertThat(deleteMessageRequest.receiptHandle()).isEqualTo("handle")

        verify(sqsClient).receiveMessage(any<ReceiveMessageRequest>())
    }

    @Test
    fun shouldNotDeleteMessageWhenProcessorThrowsException(){
        val eventId = 1L
        val eventType = InboxEventType.NOTIFICATION_REQUESTED
        val aggregateType = InboxAggregateType.MEDICAL_ORDER
        val aggregateId = 10L
        val createdAt = LocalDateTime.now()
        val payload = JsonNodeFactory.instance.objectNode()
        val retrievedEvent = RetrievedEvent(eventId, eventType, aggregateType, aggregateId, createdAt, payload)

        val response = ReceiveMessageResponse
            .builder().messages(
                Message.builder()
                    .body(objectMapper.writeValueAsString(retrievedEvent))
                    .receiptHandle("handle")
                    .build())
            .build()

        whenever(sqsClient.receiveMessage(any<ReceiveMessageRequest>())).thenReturn(response)
        doThrow(IllegalArgumentException())
            .whenever(eventProcessor).process(any<RetrievedEvent>())

        sqsMessageReceiver.receiveMessage()

        verify(sqsClient, never()).deleteMessage(any<DeleteMessageRequest>())
        verify(eventProcessor).process(any<RetrievedEvent>())
    }

    @Test
    fun shouldNotDeleteMessageWhenDeserializationFails(){

        val response = ReceiveMessageResponse
            .builder().messages(
                Message.builder()
                    .body("")
                    .receiptHandle("handle")
                    .build())
            .build()

        whenever(sqsClient.receiveMessage(any<ReceiveMessageRequest>())).thenReturn(response)
        sqsMessageReceiver.receiveMessage()
        verify(sqsClient, never()).deleteMessage(any<DeleteMessageRequest>())
        verify(eventProcessor, never()).process(any<RetrievedEvent>())
    }
}