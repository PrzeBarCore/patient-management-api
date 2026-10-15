package com.przebarcore.notificationservice.delivery.sender

import com.przebarcore.notificationservice.dto.NotificationDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class LoggingNotificationSender : NotificationSender {
    companion object{
        private val logger = LoggerFactory.getLogger(LoggingNotificationSender::class.java)
    }
    override fun send(notificationDto: NotificationDto) {
        logger.info(notificationDto.message)
    }
}