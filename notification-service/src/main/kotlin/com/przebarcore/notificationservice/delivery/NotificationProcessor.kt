package com.przebarcore.notificationservice.delivery

import com.przebarcore.notificationservice.configuration.NotificationDeliveryProperties
import com.przebarcore.notificationservice.delivery.sender.NotificationSender
import com.przebarcore.notificationservice.global.enums.NotificationStatus
import com.przebarcore.notificationservice.repository.NotificationRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDateTime
import java.util.UUID

@Component
class NotificationProcessor(
    private val notificationSender: NotificationSender,
    private val notificationRepository: NotificationRepository,
    private val clock: Clock,
    private val properties: NotificationDeliveryProperties,
    private val messageFactory: NotificationMessageFactory
) {

    companion object{
        private val logger = LoggerFactory.getLogger(NotificationProcessor::class.java)
    }

    @Transactional
    fun processNotification(notificationId : UUID){
        val now = LocalDateTime.now(clock)
        val notification = notificationRepository.findById(notificationId)
            .orElseThrow { IllegalArgumentException("Notification $notificationId does not exist") }

        val message = messageFactory.create(notification)
        try{
            notificationSender.send(message)
        } catch (e : Exception){
            logger.error("Sender: {} returned exception for notification: {}",notificationSender::class.simpleName, notificationId, e)
            notification.attemptCount++
            if(notification.attemptCount < properties.maxAttemptsCount){
                notification.nextAttemptAt = now.plusSeconds(30L * notification.attemptCount)
            } else {
                notification.status = NotificationStatus.FAILED
                notification.processedAt = now
                notification.nextAttemptAt = null
            }

            val prefix = "Sender: ${notificationSender::class.simpleName} returned exception: "
            val availableLength = 200 - prefix.length

            notification.lastError =
                prefix + (e.message ?: "Unknown error").take(availableLength)
            return
        }

        notification.lastError = null
        notification.nextAttemptAt = null
        notification.attemptCount++
        notification.status = NotificationStatus.SENT
        notification.processedAt = now
    }


}