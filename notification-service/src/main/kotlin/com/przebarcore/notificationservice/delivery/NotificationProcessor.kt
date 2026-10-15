package com.przebarcore.notificationservice.delivery

import com.przebarcore.notificationservice.delivery.sender.NotificationSender
import com.przebarcore.notificationservice.dto.NotificationDto
import com.przebarcore.notificationservice.entity.Notification
import com.przebarcore.notificationservice.global.enums.NotificationStatus
import com.przebarcore.notificationservice.global.enums.NotificationType
import com.przebarcore.notificationservice.global.enums.ReferenceType
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
    private val clock: Clock
) {

    companion object{
        private val logger = LoggerFactory.getLogger(NotificationProcessor::class.java)
    }

    @Transactional
    fun processNotification(notificationId : UUID){
        val notification = notificationRepository.findById(notificationId)
            .orElseThrow { IllegalArgumentException("Notification $notificationId does not exist") }

        val message = prepareMessage(notification)
        if(message.isEmpty()){
            logger.error("Message could not be prepared for notification: {}", notificationId)
            notification.status = NotificationStatus.FAILED
            notification.processedAt = LocalDateTime.now(clock)
            return
        }

        try{
            notificationSender.send(NotificationDto(message))
        } catch (e : Exception){
            logger.error("Sender: {} returned exception for notification: {}",notificationSender::class.simpleName, notificationId, e)
            notification.status = NotificationStatus.FAILED
            notification.processedAt = LocalDateTime.now(clock)
            return
        }

        notification.status = NotificationStatus.SENT
        notification.processedAt = LocalDateTime.now(clock)
    }

    private fun prepareMessage(notification: Notification): String {
        when(notification.referenceType) {
            ReferenceType.MEDICAL_ORDER -> {
                return when(notification.notificationType){
                    NotificationType.REMINDER -> "Order ${notification.referenceId} should be processed in 6 hours"
                    NotificationType.ESCALATION -> "Deadline for order ${notification.referenceId} has passed!"
                }
            }
        }
    }
}