package com.przebarcore.notificationservice.observability

import com.przebarcore.notificationservice.global.enums.NotificationStatus
import com.przebarcore.notificationservice.repository.NotificationRepository
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import kotlin.test.Test

@ExtendWith(MockitoExtension::class)
class NotificationMetricsTest {

    @Mock
    private lateinit var repository: NotificationRepository

    private lateinit var meterRegistry: SimpleMeterRegistry
    private lateinit var metrics: NotificationMetrics

    @BeforeEach
    fun setUp() {
        meterRegistry = SimpleMeterRegistry()
        metrics = NotificationMetrics(repository, meterRegistry)

        metrics.bindTo(meterRegistry)
    }

    @Test
    fun shouldRegisterSuccessfulDelivery() {
        val sample = metrics.start()

        metrics.registerSuccess(sample)

        val counter = meterRegistry
            .get("notification.delivery.attempts")
            .tag("result", "success")
            .counter()

        val timer = meterRegistry
            .get("notification.delivery.duration")
            .tag("result", "success")
            .timer()

        assertThat(counter.count()).isEqualTo(1.0)
        assertThat(timer.count()).isEqualTo(1)
    }

    @Test
    fun shouldRegisterFailedDelivery() {
        val sample = metrics.start()

        metrics.registerFailure(sample)

        val counter = meterRegistry
            .get("notification.delivery.attempts")
            .tag("result", "failure")
            .counter()

        val timer = meterRegistry
            .get("notification.delivery.duration")
            .tag("result", "failure")
            .timer()

        assertThat(counter.count()).isEqualTo(1.0)
        assertThat(timer.count()).isEqualTo(1)
    }

    @Test
    fun shouldReportNumberOfPendingNotifications() {
        whenever(repository.countByStatus(NotificationStatus.PENDING))
            .thenReturn(3L)

        val gauge = meterRegistry
            .get("notification.pending")
            .gauge()

        assertThat(gauge.value()).isEqualTo(3.0)
    }
}