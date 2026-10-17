package com.przebarcore.notificationservice.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "notification-delivery")
data class NotificationDeliveryProperties(
    val senderType: String,
    val schedulerEnabled: Boolean,
    val schedulerFixedDelay: Long,
    val maxAttemptsCount: Int,
    val email: EmailProperties
)

data class EmailProperties(
    val recipient: String,
    val from: String
)