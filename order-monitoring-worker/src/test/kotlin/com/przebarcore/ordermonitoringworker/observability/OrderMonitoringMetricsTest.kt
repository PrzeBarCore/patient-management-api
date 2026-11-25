package com.przebarcore.ordermonitoringworker.observability

import com.przebarcore.ordermonitoringworker.global.enums.MonitoringStatus
import com.przebarcore.ordermonitoringworker.repository.MonitoredOrderRepository
import com.przebarcore.ordermonitoringworker.repository.OutboxEventRepository
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class OrderMonitoringMetricsTest {
    @Mock
    private lateinit var monitoredOrderRepository: MonitoredOrderRepository
    @Mock
    private lateinit var outboxEventRepository: OutboxEventRepository
    private lateinit var meterRegistry: SimpleMeterRegistry
    private lateinit var metrics: OrderMonitoringMetrics

    @BeforeEach
    fun setUp() {
        meterRegistry = SimpleMeterRegistry()
        metrics = OrderMonitoringMetrics(meterRegistry, monitoredOrderRepository, outboxEventRepository)
        metrics.bindTo(meterRegistry)
    }

    @ParameterizedTest
    @CsvSource(
        "WARNING, reminder",
        "OVERDUE, escalation",
        "ON_TIME, no_action"
    )
    fun shouldRegisterOrderStatus(
        status: MonitoringStatus,
        expectedTag: String
    ) {
        metrics.registerOrdersState(status)

        val counter = meterRegistry
            .get("order.monitoring.checks")
            .tag("result", expectedTag)
            .counter()

        assertThat(counter.count()).isEqualTo(1.0)
    }

    @Test
    fun shouldRegisterTime() {
        val sample= metrics.start()

        metrics.registerProcessingDuration(sample)

        val timer = meterRegistry
            .get("order.monitoring.processing.duration")
            .timer()

        assertThat(timer.count()).isEqualTo(1)
    }

    @Test
    fun shouldReturnCountOfPendingEvents() {
        whenever(outboxEventRepository.countByProcessedAtIsNull()).thenReturn(3,5,0)

        val gauge = meterRegistry.get("order.monitoring.outbox.pending").gauge()

        assertThat(gauge.value()).isEqualTo(3.0)
        assertThat(gauge.value()).isEqualTo(5.0)
        assertThat(gauge.value()).isEqualTo(0.0)
    }

    @Test
    fun shouldReturnCountOfPendingOrders() {
        whenever(monitoredOrderRepository.countByMonitoringStatusNot(MonitoringStatus.CLOSED)).thenReturn(3)

        val gauge = meterRegistry.get("order.monitoring.active").gauge()

        assertThat(gauge.value()).isEqualTo(3.0)
    }

    @Test
    fun shouldRegisterCountersIndependently(){
        metrics.registerOrdersState(MonitoringStatus.WARNING)
        metrics.registerOrdersState(MonitoringStatus.WARNING)
        metrics.registerOrdersState(MonitoringStatus.WARNING)
        metrics.registerOrdersState(MonitoringStatus.OVERDUE)
        metrics.registerOrdersState(MonitoringStatus.OVERDUE)
        metrics.registerOrdersState(MonitoringStatus.ON_TIME)

        val reminderCount = meterRegistry.get("order.monitoring.checks").tag( "result", "reminder").counter().count()
        val escalationCount = meterRegistry.get("order.monitoring.checks").tag("result", "escalation").counter().count()
        val noActionCount = meterRegistry.get("order.monitoring.checks").tag("result", "no_action").counter().count()

        assertThat(reminderCount).isEqualTo(3.0)
        assertThat(escalationCount).isEqualTo(2.0)
        assertThat(noActionCount).isEqualTo(1.0)
    }
}