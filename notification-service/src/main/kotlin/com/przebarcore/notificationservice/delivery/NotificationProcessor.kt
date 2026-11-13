package com.przebarcore.notificationservice.delivery

import com.przebarcore.notificationservice.configuration.NotificationDeliveryProperties
import com.przebarcore.notificationservice.delivery.sender.NotificationSender
import com.przebarcore.notificationservice.global.enums.NotificationStatus
import com.przebarcore.notificationservice.observability.NotificationMetrics
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
    private val messageFactory: NotificationMessageFactory,
    private val notificationMetrics: NotificationMetrics
) {

    companion object{
        private val logger = LoggerFactory.getLogger(NotificationProcessor::class.java)
    }

    @Transactional
    fun processNotification(notificationId : UUID){

        val notification = notificationRepository.findById(notificationId)
            .orElseThrow { IllegalArgumentException("Notification $notificationId does not exist") }
        var now: LocalDateTime;
        val message = messageFactory.create(notification)
        val sample = notificationMetrics.start()
        try{
            notificationSender.send(message)
            notificationMetrics.registerSuccess(sample)
            now = LocalDateTime.now(clock)

        } catch (e : Exception){
            notificationMetrics.registerFailure(sample)
            logger.error("Sender: {} returned exception for notification: {}",notificationSender::class.simpleName, notificationId, e)
            now = LocalDateTime.now(clock)
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