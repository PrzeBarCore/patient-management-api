package com.przebarcore.notificationservice.delivery

import com.przebarcore.notificationservice.configuration.EmailProperties
import com.przebarcore.notificationservice.configuration.NotificationDeliveryProperties
import com.przebarcore.notificationservice.delivery.sender.NotificationSender
import com.przebarcore.notificationservice.delivery.model.NotificationMessage
import com.przebarcore.notificationservice.entity.Notification
import com.przebarcore.notificationservice.global.enums.NotificationStatus
import com.przebarcore.notificationservice.global.enums.NotificationType
import com.przebarcore.notificationservice.global.enums.ReferenceType
import com.przebarcore.notificationservice.repository.NotificationRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Optional
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class NotificationProcessorTest {
    @Mock
    private lateinit var notificationSender: NotificationSender
    @Mock
    private lateinit var notificationRepository: NotificationRepository

    private val messageFactory = NotificationMessageFactory()

    private val fixedClock= Clock.fixed(
        Instant.parse("2026-10-14T12:00:00.00Z"),
        ZoneId.of("UTC"))
    private val now= LocalDateTime.now(fixedClock)

    private lateinit var notificationProcessor: NotificationProcessor

    private val maxAttemptsCount=2

    private val properties = NotificationDeliveryProperties(
        "email",
        false,
        5000,
        maxAttemptsCount,
        EmailProperties("to","from" ))

    @BeforeEach
    fun setUp() {
        notificationProcessor = NotificationProcessor(notificationSender, notificationRepository, fixedClock, properties, messageFactory)
    }

    @Test
    fun shouldSetSentWhenNotificationIsSuccessfullyProcessed(){
        val requestId1 = UUID.randomUUID()
        val referenceType1 = ReferenceType.MEDICAL_ORDER
        val referenceId1 = 10L
        val notificationType1 = NotificationType.ESCALATION
        val status1 = NotificationStatus.PENDING
        val createdAt1 = now.minusHours(2)
        val notification1 = Notification(requestId1, referenceType1, referenceId1, notificationType1, status1, createdAt1, null, 0, null, null)

        val requestId2 = UUID.randomUUID()
        val referenceType2 = ReferenceType.MEDICAL_ORDER
        val referenceId2 = 11L
        val notificationType2 = NotificationType.REMINDER
        val status2 = NotificationStatus.PENDING
        val createdAt2 = now.minusHours(3)
        val notification2 = Notification(requestId2, referenceType2, referenceId2, notificationType2, status2, createdAt2, null, 0, null, null)

        whenever(notificationRepository.findById(requestId1)).thenReturn(Optional.of(notification1))
        whenever(notificationRepository.findById(requestId2)).thenReturn(Optional.of(notification2))

        notificationProcessor.processNotification(requestId1)
        notificationProcessor.processNotification(requestId2)

        val captor = argumentCaptor<NotificationMessage>()
        verify(notificationSender, times(2)).send(captor.capture())

        assertThat(captor.firstValue.content).isEqualTo("Deadline for order $referenceId1 has passed!")
        assertThat(captor.secondValue.content).isEqualTo("Order $referenceId2 should be processed in 6 hours")
        assertThat(notification1.status).isEqualTo(NotificationStatus.SENT)
        assertThat(notification1.processedAt).isEqualTo(now)
        assertThat(notification1.attemptCount).isEqualTo(1)
        assertThat(notification1.lastError).isNull()
        assertThat(notification1.nextAttemptAt).isNull()
        assertThat(notification2.status).isEqualTo(NotificationStatus.SENT)
        assertThat(notification2.processedAt).isEqualTo(now)
        assertThat(notification2.attemptCount).isEqualTo(1)
        assertThat(notification2.lastError).isNull()
        assertThat(notification2.nextAttemptAt).isNull()

        verify(notificationRepository).findById(requestId1)
        verify(notificationRepository).findById(requestId2)
    }

    @Test
    fun shouldThrowExceptionWhenNotificationDoesNotExist(){
        val requestId1 = UUID.randomUUID()

        whenever(notificationRepository.findById(requestId1)).thenReturn(Optional.empty())

        val exception = assertThrows(IllegalArgumentException::class.java) { notificationProcessor.processNotification(requestId1) }

        assertThat(exception.message).isEqualTo("Notification $requestId1 does not exist")

        verify(notificationRepository).findById(requestId1)
        verify(notificationSender, never()).send(any<NotificationMessage>())
    }
}