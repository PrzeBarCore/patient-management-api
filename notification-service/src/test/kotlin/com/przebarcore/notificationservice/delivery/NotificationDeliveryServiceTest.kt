package com.przebarcore.notificationservice.delivery

import com.przebarcore.notificationservice.global.enums.NotificationStatus
import com.przebarcore.notificationservice.repository.NotificationRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class NotificationDeliveryServiceTest {
    @Mock
    private lateinit var notificationRepository: NotificationRepository
    @Mock
    private lateinit var notificationProcessor: NotificationProcessor
    private lateinit var service: NotificationDeliveryService
    private val fixedClock = Clock.fixed(Instant.parse("2026-10-15T12:00:00.00Z"), ZoneId.of("UTC"))
    private val now = LocalDateTime.now(fixedClock)
    @BeforeEach
    fun setUp(){
        service = NotificationDeliveryService(notificationRepository, notificationProcessor, fixedClock)
    }
    @Test
    fun shouldInvokeProcessorForEveryFoundId(){
        val firstId = UUID.randomUUID()
        val secondId = UUID.randomUUID()
        val thirdId = UUID.randomUUID()

        whenever(notificationRepository.findAllRequestIdByStatusForPendingNotification(NotificationStatus.PENDING,
            now)).thenReturn(listOf(firstId, secondId, thirdId))

        service.processPendingNotifications()

        verify(notificationProcessor).processNotification(firstId)
        verify(notificationProcessor).processNotification(secondId)
        verify(notificationProcessor).processNotification(thirdId)
        verify(notificationRepository).findAllRequestIdByStatusForPendingNotification(NotificationStatus.PENDING, now)
    }

    @Test
    fun shouldInvokeProcessorEvenIfOneProcessFailed(){
        val firstId = UUID.randomUUID()
        val secondId = UUID.randomUUID()
        val thirdId = UUID.randomUUID()

        whenever(notificationRepository.findAllRequestIdByStatusForPendingNotification(NotificationStatus.PENDING, now))
            .thenReturn(listOf(firstId, secondId, thirdId))

        doNothing()
            .whenever(notificationProcessor).processNotification(firstId)

        doThrow(IllegalArgumentException("Notification $secondId does not exist"))
            .whenever(notificationProcessor).processNotification(secondId)

        doNothing()
            .whenever(notificationProcessor).processNotification(thirdId)

        service.processPendingNotifications()

        verify(notificationProcessor).processNotification(firstId)
        verify(notificationProcessor).processNotification(secondId)
        verify(notificationProcessor).processNotification(thirdId)
        verify(notificationRepository).findAllRequestIdByStatusForPendingNotification(NotificationStatus.PENDING, now)
    }
}