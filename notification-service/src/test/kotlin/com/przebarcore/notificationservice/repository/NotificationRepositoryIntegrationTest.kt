package com.przebarcore.notificationservice.repository

import com.przebarcore.notificationservice.entity.Notification
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
@SpringBootTest(
    properties = ["notification-delivery.scheduler-enabled=false"]
)
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

    private val now = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)

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
        assertThat(notification.attemptCount).isEqualTo(0)
        assertThat(notification.nextAttemptAt).isNull()
        assertThat(notification.lastError).isNull()
    }

    @Test
    fun shouldFindPendingNotificationsReadyForProcessing(){
        val notificationType = NotificationType.ESCALATION
        val status = NotificationStatus.PENDING
        val referenceType = ReferenceType.MEDICAL_ORDER
        val createdAt = now.minusHours(2)

        val requestId1 = UUID.randomUUID()
        val referenceId1 = 1L
        val nextAttemptAt1 = now.plusSeconds(30)
        val notification1 = Notification(requestId1, referenceType, referenceId1, notificationType, status, createdAt, null, 1, nextAttemptAt1, "")

        val requestId2 = UUID.randomUUID()
        val referenceId2 = 2L
        val nextAttemptAt2 = now
        val notification2 = Notification(requestId2, referenceType, referenceId2, notificationType, status, createdAt, null, 1, nextAttemptAt2, "")

        val requestId3 = UUID.randomUUID()
        val referenceId3 = 3L
        val nextAttemptAt3 = now.minusSeconds(1)
        val notification3 = Notification(requestId3, referenceType, referenceId3, notificationType, status, createdAt, null, 2, nextAttemptAt3, "")

        val requestId4 = UUID.randomUUID()
        val referenceId4 = 4L
        val nextAttemptAt4 = null
        val notification4 = Notification(requestId4, referenceType, referenceId4, notificationType, status, createdAt, null, 0, nextAttemptAt4, null)

        val requestId5 = UUID.randomUUID()
        val referenceId5 = 5L
        val nextAttemptAt5 = null
        val status5 = NotificationStatus.SENT
        val processedAt5 = now.minusDays(1)
        val notification5 = Notification(requestId5, referenceType, referenceId5, notificationType, status5, createdAt, processedAt5, 0, nextAttemptAt5, null)

        val requestId6 = UUID.randomUUID()
        val referenceId6 = 6L
        val nextAttemptAt6 = null
        val status6 = NotificationStatus.FAILED
        val processedAt6 = now.minusDays(1)
        val notification6 = Notification(requestId6, referenceType, referenceId6, notificationType, status6, createdAt, processedAt6, 3, nextAttemptAt6, "")

        notificationRepository.saveAll(listOf(notification1, notification2, notification3, notification4, notification5, notification6))

        val foundNotifications = notificationRepository.findAllRequestIdByStatusForPendingNotification(NotificationStatus.PENDING, now)

        assertThat(foundNotifications).containsExactlyInAnyOrder(requestId2,requestId3,requestId4)
    }
}