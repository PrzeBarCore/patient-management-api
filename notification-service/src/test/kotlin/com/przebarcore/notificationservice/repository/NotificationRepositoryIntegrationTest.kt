package com.przebarcore.notificationservice.repository

import com.przebarcore.notificationservice.global.enums.NotificationStatus
import com.przebarcore.notificationservice.global.enums.NotificationType
import com.przebarcore.notificationservice.global.enums.ReferenceType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

@Transactional
@SpringBootTest
@Testcontainers
class NotificationRepositoryIntegrationTest {

    companion object {
        @ServiceConnection
        @Container
        @JvmStatic
        private val postgres = PostgreSQLContainer(DockerImageName.parse("postgres:16"))
    }
    @Autowired
    private lateinit var notificationRepository: NotificationRepository


    @BeforeEach
    fun setUp() {
        notificationRepository.deleteAll()
    }

    @Test
    fun shouldCreateOnlyOneNotificationForSameRequestId(){
        val notificationRequestedEventId = UUID.randomUUID()
        val referenceType = ReferenceType.MEDICAL_ORDER
        val orderId = 1L
        val notificationType = NotificationType.ESCALATION

        val now = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
        val createdAt = now.minusHours(1)


        val firstResult = notificationRepository.tryCreateNotification(notificationRequestedEventId, referenceType.name, orderId, notificationType.name, createdAt)
        assertThat(firstResult).isEqualTo(1)

        val secondResult = notificationRepository.tryCreateNotification(notificationRequestedEventId, referenceType.name, orderId, notificationType.name, createdAt)
        assertThat(secondResult).isEqualTo(0)

        val notification = notificationRepository.findById(notificationRequestedEventId).orElseThrow()

        assertThat(notification.createdAt).isEqualTo(createdAt)
        assertThat(notification.notificationType).isEqualTo(notificationType)
        assertThat(notification.referenceType).isEqualTo(referenceType)
        assertThat(notification.referenceId).isEqualTo(orderId)
        assertThat(notification.status).isEqualTo(NotificationStatus.PENDING)
        assertThat(notification.processedAt).isNull()
    }
}