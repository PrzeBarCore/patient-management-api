package com.przebarcore.notificationservice.observability

import com.przebarcore.notificationservice.global.enums.NotificationStatus
import com.przebarcore.notificationservice.repository.NotificationRepository
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import io.micrometer.core.instrument.Timer.Sample
import io.micrometer.core.instrument.binder.MeterBinder
import org.springframework.stereotype.Component

@Component
class NotificationMetrics(
    private val notificationRepository: NotificationRepository,
    private val meterRegistry: MeterRegistry) : MeterBinder {
    override fun bindTo(registry: MeterRegistry) {
        Gauge.builder(
            "notification.pending",
            notificationRepository
        ) { repository ->
            repository.countByStatus(NotificationStatus.PENDING).toDouble()
        }
            .description("Current number of pending notifications")
            .register(registry)
    }

    fun start(): Sample =
        Timer.start(meterRegistry)

    fun registerSuccess(sample: Sample) {
        sample?.stop(
            meterRegistry.timer(
                "notification.delivery.duration",
                "result", "success"
            )
        )

        meterRegistry.counter(
            "notification.delivery.attempts",
            "result", "success"
        ).increment()
    }

    fun registerFailure(sample: Sample){
        sample?.stop(
            meterRegistry.timer(
                "notification.delivery.duration",
                "result", "failure"
            )
        )
        meterRegistry.counter(
            "notification.delivery.attempts",
            "result", "failure"
        ).increment()
    }
}