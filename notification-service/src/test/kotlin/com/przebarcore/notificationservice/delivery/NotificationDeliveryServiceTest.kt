package com.przebarcore.notificationservice.delivery

import com.przebarcore.notificationservice.global.enums.NotificationStatus
import com.przebarcore.notificationservice.repository.NotificationRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class NotificationDeliveryServiceTest {
    @Mock
    private lateinit var notificationRepository: NotificationRepository
    @Mock
    private lateinit var notificationProcessor: NotificationProcessor
    @InjectMocks
    private lateinit var service: NotificationDeliveryService

    @Test
    fun shouldInvokeProcessorForEveryFoundId(){
        val firstId = UUID.randomUUID()
        val secondId = UUID.randomUUID()
        val thirdId = UUID.randomUUID()

        whenever(notificationRepository.findAllRequestIdByStatus(NotificationStatus.PENDING))
            .thenReturn(listOf(firstId, secondId, thirdId))

        service.processPendingNotifications()

        verify(notificationProcessor).processNotification(firstId)
        verify(notificationProcessor).processNotification(secondId)
        verify(notificationProcessor).processNotification(thirdId)
        verify(notificationRepository).findAllRequestIdByStatus(NotificationStatus.PENDING)
    }

    @Test
    fun shouldInvokeProcessorEvenIfOneProcessFailed(){
        val firstId = UUID.randomUUID()
        val secondId = UUID.randomUUID()
        val thirdId = UUID.randomUUID()

        whenever(notificationRepository.findAllRequestIdByStatus(NotificationStatus.PENDING))
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
        verify(notificationRepository).findAllRequestIdByStatus(NotificationStatus.PENDING)
    }
}