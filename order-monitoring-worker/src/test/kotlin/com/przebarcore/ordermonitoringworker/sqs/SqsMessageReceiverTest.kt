package com.przebarcore.ordermonitoringworker.sqs

import com.przebarcore.ordermonitoringworker.dto.PublishedEvent
import com.przebarcore.ordermonitoringworker.global.enums.AggregateType
import com.przebarcore.ordermonitoringworker.global.enums.EventType
import com.przebarcore.ordermonitoringworker.processing.EventProcessor
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.node.JsonNodeFactory

import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest
import software.amazon.awssdk.services.sqs.model.Message
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import java.time.LocalDateTime
import kotlin.test.Test

@ExtendWith(MockitoExtension::class)
class SqsMessageReceiverTest {
    @Mock
    lateinit var objectMapper: ObjectMapper
    @Mock
    lateinit var sqsClient: SqsClient
    @Mock
    lateinit var eventProcessor: EventProcessor
    private lateinit var sqsMessageReceiver: SqsMessageReceiver

    @BeforeEach
    fun setUp() {
        sqsMessageReceiver = SqsMessageReceiver(objectMapper, sqsClient, eventProcessor, "testQueueUrl")
    }

    @Test
    fun shouldDeleteMessageAfterSuccessfulProcessing() {
        val publishedEvent = PublishedEvent(
            1L,
            EventType.MEDICAL_ORDER_STATUS_CHANGED,
            AggregateType.MEDICAL_ORDER,
            1L,
            LocalDateTime.now(),
            JsonNodeFactory.instance.objectNode()
        )
        val attempts = listOf(publishedEvent)
        for(attempt in attempts) {
        val message = Message.builder()
            .messageId("message-1")
            .body("some-json")
            .receiptHandle("receipt-handle-1")
            .build()

        whenever(sqsClient.receiveMessage(any<ReceiveMessageRequest>())).thenReturn(
            ReceiveMessageResponse.builder()
                .messages(message)
                .build()
        )

        whenever(objectMapper.readValue(eq("some-json"), eq(PublishedEvent::class.java))).thenReturn(publishedEvent)

        sqsMessageReceiver.receiveMessages()

        verify(eventProcessor).process(publishedEvent)

        val captor = argumentCaptor<DeleteMessageRequest>()
        verify(sqsClient).deleteMessage(captor.capture())
        val deleteRequest = captor.firstValue
        assertThat(deleteRequest.queueUrl()).isEqualTo("testQueueUrl")
        assertThat(deleteRequest.receiptHandle()).isEqualTo("receipt-handle-1")
        }
    }

    @Test
    fun shouldNotDeleteMessageWhenExceptionIsThrown() {
        val publishedEvent = PublishedEvent(
            1L,
            EventType.MEDICAL_ORDER_STATUS_CHANGED,
            AggregateType.MEDICAL_ORDER,
            1L,
            LocalDateTime.now(),
            JsonNodeFactory.instance.objectNode()
        )

        val message = Message.builder()
            .messageId("message-1")
            .body("some-json")
            .receiptHandle("receipt-handle-1")
            .build()

        whenever(sqsClient.receiveMessage(any<ReceiveMessageRequest>()))
            .thenReturn(ReceiveMessageResponse.builder().messages(message).build())
        whenever(objectMapper.readValue(eq("some-json"), eq(PublishedEvent::class.java)))
            .thenReturn(publishedEvent)
        doThrow(RuntimeException("Processing failed")).whenever(eventProcessor).process(any())

        sqsMessageReceiver.receiveMessages()

        verify(eventProcessor).process(publishedEvent)
        verify(sqsClient, never()).deleteMessage(any<DeleteMessageRequest>())
    }
}