package com.przebarcore.notificationservice.delivery

import com.przebarcore.notificationservice.delivery.sender.NotificationSender
import com.przebarcore.notificationservice.dto.NotificationDto
import com.przebarcore.notificationservice.entity.Notification
import com.przebarcore.notificationservice.global.enums.NotificationStatus
import com.przebarcore.notificationservice.global.enums.NotificationType
import com.przebarcore.notificationservice.global.enums.ReferenceType
import com.przebarcore.notificationservice.repository.NotificationRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.UUID

@SpringBootTest(
    properties = ["notification-monitoring.scheduler-enabled=false"]
)
@Testcontainers
@Import(NotificationProcessorIntegrationTest.FixedClockConfig::class)
class NotificationProcessorIntegrationTest {

    companion object{
        @Container
        @ServiceConnection
        @JvmStatic
        private val postgres = PostgreSQLContainer(DockerImageName.parse("postgres:16"))
    }
    @MockitoBean
    private lateinit var notificationSender: NotificationSender
    @Autowired
    private lateinit var notificationRepository: NotificationRepository
    @Autowired
    private lateinit var notificationProcessor: NotificationProcessor

    @TestConfiguration
    class FixedClockConfig {
        @Bean
        @Primary
        fun fixedClock(): Clock =
            Clock.fixed(
                Instant.parse("2026-10-14T12:00:00Z"),
                ZoneId.of("UTC")
            )
    }

    private val now =LocalDateTime.parse("2026-10-14T12:00:00").truncatedTo(ChronoUnit.MILLIS)

    @BeforeEach
    fun setUp() {
        notificationRepository.deleteAll()
    }

    @Test
    fun shouldSaveSentStatusIfSendingProcessWasSuccessful(){
        val requestId1 = UUID.randomUUID()
        val referenceType1 = ReferenceType.MEDICAL_ORDER
        val referenceId1 = 10L
        val notificationType1 = NotificationType.ESCALATION
        val status1 = NotificationStatus.PENDING
        val createdAt1 = now.minusHours(2)
        val notification1 = Notification(requestId1, referenceType1, referenceId1, notificationType1, status1, createdAt1, null)

        notificationRepository.save(notification1)

        notificationProcessor.processNotification(requestId1)

        val savedNotification = notificationRepository.findById(requestId1).orElseThrow()

        assertThat(savedNotification.status).isEqualTo(NotificationStatus.SENT)
        assertThat(savedNotification.processedAt).isEqualTo(now)
        assertThat(savedNotification.referenceType).isEqualTo(referenceType1)
        assertThat(savedNotification.referenceId).isEqualTo(referenceId1)
        assertThat(savedNotification.notificationType).isEqualTo(notificationType1)
        assertThat(savedNotification.createdAt).isEqualTo(createdAt1)

        verify(notificationSender).send(any<NotificationDto>())
    }

    @Test
    fun shouldSaveFailedStatusWhenSenderThrowsException(){
        val requestId1 = UUID.randomUUID()
        val referenceType1 = ReferenceType.MEDICAL_ORDER
        val referenceId1 = 10L
        val notificationType1 = NotificationType.ESCALATION
        val status1 = NotificationStatus.PENDING
        val createdAt1 = now.minusHours(2)
        val notification1 = Notification(requestId1, referenceType1, referenceId1, notificationType1, status1, createdAt1, null)

        notificationRepository.save(notification1)

        doThrow(RuntimeException("Sender no more")).whenever(notificationSender).send(any<NotificationDto>())

        notificationProcessor.processNotification(requestId1)

        val savedNotification = notificationRepository.findById(requestId1).orElseThrow()

        assertThat(savedNotification.status).isEqualTo(NotificationStatus.FAILED)
        assertThat(savedNotification.processedAt).isEqualTo(now)
        assertThat(savedNotification.referenceType).isEqualTo(referenceType1)
        assertThat(savedNotification.referenceId).isEqualTo(referenceId1)
        assertThat(savedNotification.notificationType).isEqualTo(notificationType1)
        assertThat(savedNotification.createdAt).isEqualTo(createdAt1)

        verify(notificationSender).send(any<NotificationDto>())
    }

}