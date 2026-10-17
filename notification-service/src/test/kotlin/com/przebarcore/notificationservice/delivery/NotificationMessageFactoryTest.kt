package com.przebarcore.notificationservice.delivery

import com.przebarcore.notificationservice.entity.Notification
import com.przebarcore.notificationservice.global.enums.NotificationStatus
import com.przebarcore.notificationservice.global.enums.NotificationType
import com.przebarcore.notificationservice.global.enums.ReferenceType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

class NotificationMessageFactoryTest {
    private val factory = NotificationMessageFactory()

    @Test
    fun shouldGenerateMessageForMedicalOrderReminderNotification(){
        val notification = Notification(UUID.randomUUID(),
            ReferenceType.MEDICAL_ORDER,
            10L,
            NotificationType.REMINDER,
            NotificationStatus.PENDING,
            LocalDateTime.now(),
            null,
            0,
            null,
            null)

        val message = factory.create(notification)

        assertThat(message.content).isEqualTo("Order 10 should be processed in 6 hours")
        assertThat(message.subject).isEqualTo("Reminder: Order 10")
    }

    @Test
    fun shouldGenerateMessageForMedicalOrderEscalationNotification(){
        val notification = Notification(UUID.randomUUID(),
            ReferenceType.MEDICAL_ORDER,
            10L,
            NotificationType.ESCALATION,
            NotificationStatus.PENDING,
            LocalDateTime.now(),
            null,
            0,
            null,
            null)

        val message = factory.create(notification)

        assertThat(message.content).isEqualTo("Deadline for order 10 has passed!")
        assertThat(message.subject).isEqualTo("Escalation: Order 10!")
    }

}