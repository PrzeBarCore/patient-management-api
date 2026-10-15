package com.przebarcore.notificationservice.delivery

import com.przebarcore.notificationservice.delivery.sender.NotificationSender
import com.przebarcore.notificationservice.dto.NotificationDto
import com.przebarcore.notificationservice.entity.Notification
import com.przebarcore.notificationservice.global.enums.NotificationStatus
import com.przebarcore.notificationservice.global.enums.NotificationType
import com.przebarcore.notificationservice.global.enums.ReferenceType
import com.przebarcore.notificationservice.repository.NotificationRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDateTime
import java.util.UUID
import kotlin.math.min

@Component
class NotificationProcessor(
    private val notificationSender: NotificationSender,
    private val notificationRepository: NotificationRepository,
    private val clock: Clock,
    @Value("\${notification-delivery.max-attempts-count}")
    private val maxAttemptsCount: Int,
) {

    companion object{
        private val logger = LoggerFactory.getLogger(NotificationProcessor::class.java)
    }

    @Transactional
    fun processNotification(notificationId : UUID){
        val now = LocalDateTime.now(clock)
        val notification = notificationRepository.findById(notificationId)
            .orElseThrow { IllegalArgumentException("Notification $notificationId does not exist") }

        val message = prepareMessage(notification)

        try{
            notificationSender.send(NotificationDto(message))
        } catch (e : Exception){
            logger.error("Sender: {} returned exception for notification: {}",notificationSender::class.simpleName, notificationId, e)
            notification.attemptCount++
            if(notification.attemptCount < maxAttemptsCount){
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

    private fun prepareMessage(notification: Notification): String =
        when(notification.referenceType) {
            ReferenceType.MEDICAL_ORDER -> {
                return when(notification.notificationType){
                    NotificationType.REMINDER -> "Order ${notification.referenceId} should be processed in 6 hours"
                    NotificationType.ESCALATION -> "Deadline for order ${notification.referenceId} has passed!"
                }
            }
        }
}