package com.przebarcore.notificationservice.delivery

import com.przebarcore.notificationservice.configuration.EmailProperties
import com.przebarcore.notificationservice.configuration.NotificationDeliveryProperties
import com.przebarcore.notificationservice.delivery.sender.EmailNotificationSender
import com.przebarcore.notificationservice.delivery.model.NotificationMessage
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.mail.MailSendException
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender

@ExtendWith(MockitoExtension::class)
class EmailNotificationSenderTest {
    @Mock
    private lateinit var mockJavaMailSender: JavaMailSender

    private lateinit var emailNotificationSender: EmailNotificationSender

    private val to = "test@test.pl"
    private val from = "theBest@test.pl"
    private val properties = NotificationDeliveryProperties(
        "email",
        false,
        5000,
        1,
        EmailProperties(to,from ))
    @BeforeEach
    fun setUp() {
        emailNotificationSender = EmailNotificationSender(mockJavaMailSender, properties)
    }

    @Test
    fun shouldSendEmailNotification(){
        val notificationMessage = NotificationMessage("subject", "content")

        emailNotificationSender.send(notificationMessage)

        val argumentCaptor = argumentCaptor<SimpleMailMessage>()
        verify(mockJavaMailSender).send(argumentCaptor.capture())

        val simpleMessage = argumentCaptor.firstValue
        assertThat(simpleMessage.subject).isEqualTo(notificationMessage.subject)
        assertThat(simpleMessage.text).isEqualTo(notificationMessage.content)
        assertThat(simpleMessage.to).containsExactly(to)
        assertThat(simpleMessage.from).isEqualTo(from)
    }

    @Test
    fun shouldPropagateSenderException(){
        val notificationMessage = NotificationMessage("subject", "content")

        doThrow(MailSendException("Pigeon didn't make it")).whenever(mockJavaMailSender).send(any<SimpleMailMessage>())

        assertThrows(MailSendException::class.java) { emailNotificationSender.send(notificationMessage) }
    }
}

