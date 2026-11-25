package com.przebarcore.ordermonitoringworker.observability

import com.przebarcore.ordermonitoringworker.global.enums.MonitoringStatus
import com.przebarcore.ordermonitoringworker.repository.MonitoredOrderRepository
import com.przebarcore.ordermonitoringworker.repository.OutboxEventRepository
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.binder.MeterBinder
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.Timer
import io.micrometer.core.instrument.Timer.Sample
import org.springframework.stereotype.Component

@Component
class OrderMonitoringMetrics(private val meterRegistry: MeterRegistry,
                             private val monitoredOrderRepository: MonitoredOrderRepository,
                             private val outboxEventRepository: OutboxEventRepository
) : MeterBinder  {

    override fun bindTo(registry: MeterRegistry) {
        Gauge.builder("order.monitoring.active",
            monitoredOrderRepository)
        { repository ->
            repository.countByMonitoringStatusNot(MonitoringStatus.CLOSED).toDouble()
        }
            .description("Current number of active orders")
            .register(registry)

        Gauge.builder("order.monitoring.outbox.pending",
            outboxEventRepository)
        { repository ->
            repository.countByProcessedAtIsNull().toDouble()
        }
            .description("Pending Outbox events")
            .register(registry)
    }

    fun start(): Sample =
        Timer.start(meterRegistry)

    fun registerOrdersState(newMonitoringStatus: MonitoringStatus) {
        val result = when (newMonitoringStatus) {
            MonitoringStatus.WARNING -> "reminder"
            MonitoringStatus.OVERDUE -> "escalation"
            else -> "no_action"}
        meterRegistry.counter("order.monitoring.checks",
        "result", result).increment()
    }

    fun registerProcessingDuration(sample: Sample) {
        sample?.stop(
            meterRegistry.timer(
                "order.monitoring.processing.duration"
            )
        )
    }
}