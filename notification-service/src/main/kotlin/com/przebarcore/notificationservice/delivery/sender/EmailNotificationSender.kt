package com.przebarcore.notificationservice.delivery.sender

import com.przebarcore.notificationservice.configuration.NotificationDeliveryProperties
import com.przebarcore.notificationservice.delivery.model.NotificationMessage
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(value = ["notification-delivery.sender-type"], havingValue = "email")
class EmailNotificationSender(
    private val mailSender: JavaMailSender,
    private val properties: NotificationDeliveryProperties)
    : NotificationSender {
    override fun send(notificationMessage: NotificationMessage) {
        val mailMessage = SimpleMailMessage()
        mailMessage.setTo(properties.email.recipient)
        mailMessage.from = properties.email.from
        mailMessage.subject = notificationMessage.subject
        mailMessage.text = notificationMessage.content

        mailSender.send(mailMessage)
        logger.info("Email notification sender sent message to ${properties.email.recipient}")
    }

    companion object{
        private val logger = LoggerFactory.getLogger(EmailNotificationSender::class.java)
    }
}