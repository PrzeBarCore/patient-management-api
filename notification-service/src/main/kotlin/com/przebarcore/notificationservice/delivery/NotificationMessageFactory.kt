package com.przebarcore.notificationservice.delivery

import com.przebarcore.notificationservice.delivery.model.NotificationMessage
import com.przebarcore.notificationservice.entity.Notification
import com.przebarcore.notificationservice.global.enums.NotificationType
import com.przebarcore.notificationservice.global.enums.ReferenceType
import org.springframework.stereotype.Component

@Component
class NotificationMessageFactory {
    fun create(notification: Notification): NotificationMessage {
        return NotificationMessage(prepareSubject(notification), prepareMessage(notification))
    }

    private fun prepareMessage(notification: Notification): String =
        when(notification.referenceType) {
            ReferenceType.MEDICAL_ORDER -> {
                when(notification.notificationType){
                    NotificationType.REMINDER -> "Order ${notification.referenceId} should be processed in 6 hours"
                    NotificationType.ESCALATION -> "Deadline for order ${notification.referenceId} has passed!"
                }
            }
        }

    private fun prepareSubject(notification: Notification): String =
        when(notification.referenceType) {
            ReferenceType.MEDICAL_ORDER -> {
                when(notification.notificationType){
                    NotificationType.REMINDER -> "Reminder: Order ${notification.referenceId}"
                    NotificationType.ESCALATION -> "Escalation: Order ${notification.referenceId}!"
                }
            }
        }


}