package com.przebarcore.notificationservice.delivery

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(
    value = ["notification-delivery.scheduler-enabled"],
    havingValue = "true"
)
class NotificationScheduler(private val notificationDeliveryService: NotificationDeliveryService) {

    @Scheduled(fixedDelayString = "\${notification-delivery.scheduler-fixed-delay}")
    fun deliverNotifications() {
        notificationDeliveryService.processPendingNotifications()
    }
}