package com.PrzeBarCore.ordermonitoringworker.service

import com.PrzeBarCore.ordermonitoringworker.dto.MedicalOrderStatusChangedEvent
import com.PrzeBarCore.ordermonitoringworker.entity.MonitoredOrder
import com.PrzeBarCore.ordermonitoringworker.global.enums.MonitoringStatus
import com.PrzeBarCore.ordermonitoringworker.global.enums.OrderStatus
import com.PrzeBarCore.ordermonitoringworker.repository.MonitoredOrderRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import java.util.Optional
import kotlin.test.Test

@ExtendWith(MockitoExtension::class)
class MedicalOrderMonitoringServiceTest{
    @Mock
    lateinit var medicalOrderRepository : MonitoredOrderRepository
    @InjectMocks
    lateinit var medicalOrderMonitoringService: MedicalOrderMonitoringService

    @Test
    fun shouldCreateNewRecord(){
        val orderId = 1L
        val statusChangedAt = LocalDateTime.now()
        val newStatus = OrderStatus.NEW
        val event = MedicalOrderStatusChangedEvent(orderId,
            OrderStatus.NEW,
            newStatus,
            statusChangedAt)

        whenever(medicalOrderRepository.findById(orderId)).thenReturn(Optional.empty())
        whenever(medicalOrderRepository.save(any<MonitoredOrder>())).thenAnswer { invocation -> invocation.getArgument<MonitoredOrder>(0) }

        medicalOrderMonitoringService.processOrderStatusChanged(event)

        val argumentCaptor = ArgumentCaptor.forClass(MonitoredOrder::class.java)

        verify(medicalOrderRepository).save(argumentCaptor.capture())
        verify(medicalOrderRepository).findById(orderId)

        val monitoredOrder = argumentCaptor.getValue()
        assertThat(monitoredOrder.orderId).isEqualTo(orderId)
        assertThat(monitoredOrder.status).isEqualTo(newStatus)
        assertThat(monitoredOrder.statusChangedAt).isEqualTo(statusChangedAt)
        assertThat(monitoredOrder.updatedAt).isNotNull()
        assertThat(monitoredOrder.monitoringStatus).isEqualTo(MonitoringStatus.ON_TIME)
        assertThat(monitoredOrder.deadlineAt).isEqualTo(statusChangedAt.plusDays(1))
    }

    @Test
    fun shouldUpdateExistingRecord(){
        val orderId = 1L
        val statusChangedAt = LocalDateTime.now()
        val newStatus = OrderStatus.IN_PROCESS
        val oldStatus = OrderStatus.NEW
        val event = MedicalOrderStatusChangedEvent(orderId,
            oldStatus,
            newStatus,
            statusChangedAt)

        val oldUpdatedAt = LocalDateTime.now().minusDays(1)
        val monitoredOrder = MonitoredOrder(
            orderId,
            oldStatus,
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1),
            MonitoringStatus.ON_TIME,
            oldUpdatedAt)

        whenever(medicalOrderRepository.findById(orderId)).thenReturn(Optional.of(monitoredOrder))

        medicalOrderMonitoringService.processOrderStatusChanged(event)

        assertThat(monitoredOrder.orderId).isEqualTo(orderId)
        assertThat(monitoredOrder.status).isEqualTo(newStatus)
        assertThat(monitoredOrder.statusChangedAt).isEqualTo(statusChangedAt)
        assertThat(monitoredOrder.updatedAt).isAfter(oldUpdatedAt)
        assertThat(monitoredOrder.monitoringStatus).isEqualTo(MonitoringStatus.ON_TIME)
        assertThat(monitoredOrder.deadlineAt).isEqualTo(statusChangedAt.plusDays(2))

        verify(medicalOrderRepository).findById(orderId)
        verify(medicalOrderRepository, never()).save(any())
    }


    @ParameterizedTest
    @EnumSource(
        value = OrderStatus::class,
        names = ["COMPLETED", "CANCELED"]
    )
    fun shouldCloseMonitoringForFinalStatus(finalStatus: OrderStatus) {
        val orderId = 1L
        val statusChangedAt = LocalDateTime.now()
        val oldStatus = OrderStatus.IN_PROCESS
        val event = MedicalOrderStatusChangedEvent(orderId,
            oldStatus,
            finalStatus,
            statusChangedAt)

        val oldUpdatedAt = LocalDateTime.now().minusDays(1)
        val monitoredOrder = MonitoredOrder(
            orderId,
            oldStatus,
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1),
            MonitoringStatus.ON_TIME,
            oldUpdatedAt)

        whenever(medicalOrderRepository.findById(orderId)).thenReturn(Optional.of(monitoredOrder))

        medicalOrderMonitoringService.processOrderStatusChanged(event)

        assertThat(monitoredOrder.orderId).isEqualTo(orderId)
        assertThat(monitoredOrder.status).isEqualTo(finalStatus)
        assertThat(monitoredOrder.statusChangedAt).isEqualTo(statusChangedAt)
        assertThat(monitoredOrder.updatedAt).isAfter(oldUpdatedAt)
        assertThat(monitoredOrder.monitoringStatus).isEqualTo(MonitoringStatus.CLOSED)
        assertThat(monitoredOrder.deadlineAt).isNull()

        verify(medicalOrderRepository).findById(orderId)
        verify(medicalOrderRepository, never()).save(any())
    }
}