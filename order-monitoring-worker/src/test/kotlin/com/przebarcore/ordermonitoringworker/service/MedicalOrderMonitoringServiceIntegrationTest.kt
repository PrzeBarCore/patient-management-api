package com.przebarcore.ordermonitoringworker.service

import com.przebarcore.ordermonitoringworker.entity.MonitoredOrder
import com.przebarcore.ordermonitoringworker.entity.OutboxEvent
import com.przebarcore.ordermonitoringworker.global.enums.MonitoringStatus
import com.przebarcore.ordermonitoringworker.global.enums.OrderStatus
import com.przebarcore.ordermonitoringworker.repository.MonitoredOrderRepository
import com.przebarcore.ordermonitoringworker.repository.OutboxEventRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.mockito.ArgumentMatchers.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.test.Test

@SpringBootTest(
    properties = ["monitoring.scheduler-enabled=false"]
)
@Testcontainers
class MedicalOrderMonitoringServiceIntegrationTest {
    //region Init
    companion object {
        @Container
        @ServiceConnection
        private val postgres = PostgreSQLContainer(DockerImageName.parse("postgres:16"))
    }

    @Autowired
    private lateinit var medicalOrderRepository: MonitoredOrderRepository

    @MockitoBean
    private lateinit var outboxEventRepository: OutboxEventRepository

    @Autowired
    private lateinit var medicalOrderMonitoringService: MedicalOrderMonitoringService

    //endregion

    //region ReviewOrdersDeadline
    @Test
    fun shouldRollbackChangesIfExceptionIsThrownForEvent() {
        val orderId = 1L
        val status = OrderStatus.NEW
        val deadlineAt = LocalDateTime.now().plusHours(2).truncatedTo(ChronoUnit.MILLIS)
        val updatedAt = deadlineAt.minusDays(1)
        val statusChangedAt = updatedAt
        val monitoringStatus = MonitoringStatus.ON_TIME
        val orderToReview = MonitoredOrder(orderId, status, statusChangedAt, deadlineAt, monitoringStatus, updatedAt)

        medicalOrderRepository.save(orderToReview)
        doThrow(RuntimeException("Have the strength and courage to be free. Join THE HELLDIVERS"))
            .whenever(outboxEventRepository)
            .save(any(OutboxEvent::class.java))

        Assertions.assertThrows(RuntimeException::class.java) {
            medicalOrderMonitoringService.reviewOrdersDeadline()
        }

        val orderAfterOperation = medicalOrderRepository.findById(orderId).orElseThrow()

        assertThat(orderAfterOperation.monitoringStatus).isEqualTo(monitoringStatus)
        assertThat(orderAfterOperation.updatedAt).isEqualTo(updatedAt)
        assertThat(orderAfterOperation.orderId).isEqualTo(orderId)
        assertThat(orderAfterOperation.status).isEqualTo(status)
        assertThat(orderAfterOperation.deadlineAt).isEqualTo(deadlineAt)
        assertThat(orderAfterOperation.statusChangedAt).isEqualTo(statusChangedAt)

        verify(outboxEventRepository).save(any())
    }
    //endregion

}