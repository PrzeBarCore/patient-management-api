package com.przebarcore.notificationservice.delivery.sender

import com.przebarcore.notificationservice.delivery.model.NotificationMessage
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(value = ["notification-delivery.sender-type"], havingValue = "logging")
class LoggingNotificationSender : NotificationSender {
    companion object{
        private val logger = LoggerFactory.getLogger(LoggingNotificationSender::class.java)
    }
    override fun send(notificationMessage: NotificationMessage) {
        logger.info(notificationMessage.content)
    }
}