package com.przebarcore.notificationservice.inbox

import com.przebarcore.notificationservice.dto.incoming.NotificationRequestedEvent
import com.przebarcore.notificationservice.dto.incoming.RetrievedEvent
import com.przebarcore.notificationservice.global.enums.InboxAggregateType
import com.przebarcore.notificationservice.global.enums.InboxEventType
import com.przebarcore.notificationservice.global.enums.NotificationType
import com.przebarcore.notificationservice.global.enums.ReferenceType
import com.przebarcore.notificationservice.repository.NotificationRepository
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import tools.jackson.core.JacksonException
import tools.jackson.databind.JsonNode
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.node.JsonNodeFactory
import tools.jackson.module.kotlin.KotlinModule
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class EventProcessorTest {
    @Mock
    private lateinit var notificationRepository : NotificationRepository
    private lateinit var eventProcessor : EventProcessor
    private val objectMapper = JsonMapper.builder()
        .addModule(KotlinModule.Builder().build())
        .build()
    @BeforeEach
    fun setUp() {
        eventProcessor = EventProcessor(objectMapper, notificationRepository)
    }

    @Test
    fun shouldCreateNotificationForNotificationRequestedEvent(){

        val notificationRequestedEventId = UUID.randomUUID()
        val referenceType = ReferenceType.MEDICAL_ORDER
        val orderId = 1L
        val notificationType = NotificationType.ESCALATION
        val now = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
        val createdAt = now.minusHours(1)

        val payload = objectMapper.valueToTree<JsonNode>(
            NotificationRequestedEvent(
                notificationRequestedEventId,
                referenceType,
                orderId,
                notificationType,
                createdAt
            )
        )

        val outboxEventId = 10L
        val aggregateType = InboxAggregateType.MEDICAL_ORDER
        val eventType = InboxEventType.NOTIFICATION_REQUESTED

        val retrievedEvent = RetrievedEvent(outboxEventId,
            eventType,
            aggregateType,
            orderId,
            createdAt,
            payload)

        whenever(notificationRepository
            .tryCreateNotification(notificationRequestedEventId, referenceType.name, orderId, notificationType.name, createdAt))
            .thenReturn(1)

        eventProcessor.process(retrievedEvent)

        verify(notificationRepository)
            .tryCreateNotification(notificationRequestedEventId, referenceType.name, orderId, notificationType.name, createdAt)
    }

    @Test
    fun shouldAcceptDuplicateNotificationRequest(){

        val notificationRequestedEventId = UUID.randomUUID()
        val referenceType = ReferenceType.MEDICAL_ORDER
        val orderId = 1L
        val notificationType = NotificationType.ESCALATION
        val now = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
        val createdAt = now.minusHours(1)

        val payload = objectMapper.valueToTree<JsonNode>(
            NotificationRequestedEvent(
                notificationRequestedEventId,
                referenceType,
                orderId,
                notificationType,
                createdAt
            )
        )

        val outboxEventId = 10L
        val aggregateType = InboxAggregateType.MEDICAL_ORDER
        val eventType = InboxEventType.NOTIFICATION_REQUESTED

        val retrievedEvent = RetrievedEvent(outboxEventId,
            eventType,
            aggregateType,
            orderId,
            createdAt,
            payload)

        whenever(notificationRepository
            .tryCreateNotification(notificationRequestedEventId, referenceType.name, orderId, notificationType.name, createdAt))
            .thenReturn(0)

        eventProcessor.process(retrievedEvent)

        verify(notificationRepository)
            .tryCreateNotification(notificationRequestedEventId, referenceType.name, orderId, notificationType.name, createdAt)
    }

//    @Test
//    fun shouldRejectUnsupportedEventType(){
//        val retrievedEvent = RetrievedEvent(1L,
//            InboxEventType.UNSUPPORTED,
//            InboxAggregateType.MEDICAL_ORDER,
//            1L,
//            LocalDateTime.now(),
//            JsonNodeFactory.instance.objectNode())
//        assertThrows(IllegalArgumentException::class.java, {eventProcessor.process(retrievedEvent)})
//
//        verify(notificationRepository, never())
//            .tryCreateNotification(any(), any(), any(), any(), any())
//    }

    @Test
    fun shouldPropagateExceptionWhenPayloadCannotBeDeserialized(){
        val retrievedEvent = RetrievedEvent(10L,
            InboxEventType.NOTIFICATION_REQUESTED,
            InboxAggregateType.MEDICAL_ORDER,
            1L,
            LocalDateTime.now(),
            JsonNodeFactory.instance.objectNode())

        assertThrows(JacksonException::class.java, {eventProcessor.process(retrievedEvent)})

        verify(notificationRepository, never())
            .tryCreateNotification(any(), any(), any(), any(), any())
    }

}