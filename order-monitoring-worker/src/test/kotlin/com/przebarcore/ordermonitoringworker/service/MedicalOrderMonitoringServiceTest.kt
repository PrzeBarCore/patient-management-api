package com.przebarcore.ordermonitoringworker.service

import com.przebarcore.ordermonitoringworker.dto.MedicalOrderCreatedEvent
import com.przebarcore.ordermonitoringworker.dto.MedicalOrderStatusChangedEvent
import com.przebarcore.ordermonitoringworker.entity.MonitoredOrder
import com.przebarcore.ordermonitoringworker.global.enums.MonitoringStatus
import com.przebarcore.ordermonitoringworker.global.enums.OrderStatus
import com.przebarcore.ordermonitoringworker.repository.MonitoredOrderRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import java.util.Optional
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

@ExtendWith(MockitoExtension::class)
class MedicalOrderMonitoringServiceTest{
    @Mock
    lateinit var medicalOrderRepository: MonitoredOrderRepository
    private lateinit var medicalOrderMonitoringService: MedicalOrderMonitoringService
    private val fixedClock = Clock.fixed(
        Instant.parse("2026-10-08T12:00:00Z"),
        ZoneOffset.UTC
    )
    val warningThresholdHours = 6L
    val now = LocalDateTime.now(fixedClock)
    @BeforeEach
    fun setUp() {
        medicalOrderMonitoringService = MedicalOrderMonitoringService(
            medicalOrderRepository,
            warningThresholdHours,
            fixedClock
        )

    }

    @Test
    fun shouldCreateNewRecord(){
        val orderId = 1L
        val statusChangedAt = now
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
        assertThat(monitoredOrder.updatedAt).isEqualTo(now)
        assertThat(monitoredOrder.monitoringStatus).isEqualTo(MonitoringStatus.ON_TIME)
        assertThat(monitoredOrder.deadlineAt).isEqualTo(statusChangedAt.plusDays(1))
    }

    @Test
    fun shouldUpdateExistingRecord(){
        val orderId = 1L
        val statusChangedAt = now
        val newStatus = OrderStatus.IN_PROCESS
        val oldStatus = OrderStatus.NEW
        val event = MedicalOrderStatusChangedEvent(orderId,
            oldStatus,
            newStatus,
            statusChangedAt)

        val oldUpdatedAt = now.minusDays(1)
        val monitoredOrder = MonitoredOrder(
            orderId,
            oldStatus,
            oldUpdatedAt,
            now.plusDays(1),
            MonitoringStatus.ON_TIME,
            oldUpdatedAt)

        whenever(medicalOrderRepository.findById(orderId)).thenReturn(Optional.of(monitoredOrder))

        medicalOrderMonitoringService.processOrderStatusChanged(event)

        assertThat(monitoredOrder.orderId).isEqualTo(orderId)
        assertThat(monitoredOrder.status).isEqualTo(newStatus)
        assertThat(monitoredOrder.statusChangedAt).isEqualTo(statusChangedAt)
        assertThat(monitoredOrder.updatedAt).isEqualTo(now)
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
        val statusChangedAt = now
        val oldStatus = OrderStatus.IN_PROCESS
        val event = MedicalOrderStatusChangedEvent(orderId,
            oldStatus,
            finalStatus,
            statusChangedAt)

        val oldUpdatedAt = now.minusDays(1)
        val monitoredOrder = MonitoredOrder(
            orderId,
            oldStatus,
            oldUpdatedAt,
            now.plusDays(1),
            MonitoringStatus.ON_TIME,
            oldUpdatedAt)

        whenever(medicalOrderRepository.findById(orderId)).thenReturn(Optional.of(monitoredOrder))

        medicalOrderMonitoringService.processOrderStatusChanged(event)

        assertThat(monitoredOrder.orderId).isEqualTo(orderId)
        assertThat(monitoredOrder.status).isEqualTo(finalStatus)
        assertThat(monitoredOrder.statusChangedAt).isEqualTo(statusChangedAt)
        assertThat(monitoredOrder.updatedAt).isEqualTo(now)
        assertThat(monitoredOrder.monitoringStatus).isEqualTo(MonitoringStatus.CLOSED)
        assertThat(monitoredOrder.deadlineAt).isNull()

        verify(medicalOrderRepository).findById(orderId)
        verify(medicalOrderRepository, never()).save(any())
    }
    @Test
    fun shouldCreateMonitoredOrderWhenOrderCreated(){
        val orderId = 1L
        val createdAt = now.minusMinutes(1)
        val status = OrderStatus.NEW
        val event = MedicalOrderCreatedEvent(orderId,
            status,
            createdAt)

        whenever(medicalOrderRepository.findById(orderId)).thenReturn(Optional.empty())
        whenever(medicalOrderRepository.save(any<MonitoredOrder>()))
            .thenAnswer { it.getArgument<MonitoredOrder>(0) }

        medicalOrderMonitoringService.processOrderCreated(event)

        val captor = ArgumentCaptor.forClass(MonitoredOrder::class.java)
        verify(medicalOrderRepository).save(captor.capture())

        val monitoredOrder= captor.value

        assertThat(monitoredOrder.orderId).isEqualTo(orderId)
        assertThat(monitoredOrder.status).isEqualTo(status)
        assertThat(monitoredOrder.statusChangedAt).isEqualTo(createdAt)
        assertThat(monitoredOrder.updatedAt).isEqualTo(now)
        assertThat(monitoredOrder.monitoringStatus).isEqualTo(MonitoringStatus.ON_TIME)
        assertThat(monitoredOrder.deadlineAt).isEqualTo(createdAt.plusDays(1))

        verify(medicalOrderRepository).findById(orderId)
    }
    @Test
    fun shouldIgnoreCreatedEventWhenOrderAlreadyExists(){
        val orderId = 1L
        val createdAt = now.minusDays(1)
        val status = OrderStatus.NEW
        val event = MedicalOrderCreatedEvent(orderId,
            status,
            createdAt)

        val existingStatus = OrderStatus.IN_PROCESS
        val existingStatusChangedAt = now.minusDays(1)
        val existingDeadline = now.plusDays(2)
        val existingUpdatedAt = now
        val existingMonitoringStatus = MonitoringStatus.ON_TIME
        val monitoredOrder = MonitoredOrder(
            orderId,
            existingStatus,
            existingStatusChangedAt,
            existingDeadline,
            existingMonitoringStatus,
            existingUpdatedAt)

        whenever(medicalOrderRepository.findById(orderId)).thenReturn(Optional.of(monitoredOrder))

        medicalOrderMonitoringService.processOrderCreated(event)

        assertThat(monitoredOrder.status).isEqualTo(existingStatus)
        assertThat(monitoredOrder.statusChangedAt).isEqualTo(existingStatusChangedAt)
        assertThat(monitoredOrder.deadlineAt).isEqualTo(existingDeadline)
        assertThat(monitoredOrder.updatedAt).isEqualTo(existingUpdatedAt)
        assertThat(monitoredOrder.monitoringStatus).isEqualTo(existingMonitoringStatus)

        verify(medicalOrderRepository, never()).save(any())
        verify(medicalOrderRepository).findById(orderId)
    }

    @Test
    fun shouldIgnoreOlderStatusChangedEvent(){
        val currentDateTime = now

        val orderId = 1L
        val statusChangedAt = currentDateTime.minusHours(2)
        val newStatus = OrderStatus.IN_PROCESS
        val oldStatus = OrderStatus.NEW
        val event = MedicalOrderStatusChangedEvent(orderId,
            oldStatus,
            newStatus,
            statusChangedAt)


        val monitoredOrderOrderId = orderId
        val monitoredOrderStatus = OrderStatus.COMPLETED
        val monitoredOrderChangedAt = currentDateTime.minusHours(1)
        val monitoredOrderDeadline = null
        val monitoredOrderMonitoringStatus= MonitoringStatus.CLOSED
        val monitoredOrderUpdatedAt = currentDateTime.minusHours(1)
        val monitoredOrder = MonitoredOrder(
            orderId,
            monitoredOrderStatus,
            monitoredOrderChangedAt,
            monitoredOrderDeadline,
            monitoredOrderMonitoringStatus,
            monitoredOrderUpdatedAt)

        whenever(medicalOrderRepository.findById(orderId)).thenReturn(Optional.of(monitoredOrder))

        medicalOrderMonitoringService.processOrderStatusChanged(event)

        assertThat(monitoredOrder.orderId).isEqualTo(monitoredOrderOrderId)
        assertThat(monitoredOrder.status).isEqualTo(monitoredOrderStatus)
        assertThat(monitoredOrder.statusChangedAt).isEqualTo(monitoredOrderChangedAt)
        assertThat(monitoredOrder.updatedAt).isEqualTo(monitoredOrderUpdatedAt)
        assertThat(monitoredOrder.monitoringStatus).isEqualTo(monitoredOrderMonitoringStatus)
        assertThat(monitoredOrder.deadlineAt).isNull()

        verify(medicalOrderRepository).findById(orderId)
        verify(medicalOrderRepository, never()).save(any())
    }

    @Test
    fun shouldSetWarningWhenDeadlineIsClose(){
        val orderId =1L
        val status = OrderStatus.NEW
        val deadlineAt = now.plusHours(2)
        val updatedAt = deadlineAt.minusDays(1)
        val statusChangedAt = updatedAt
        val monitoringStatus = MonitoringStatus.ON_TIME
        val orderToReview = MonitoredOrder(orderId, status, statusChangedAt, deadlineAt, monitoringStatus, updatedAt)

        whenever(medicalOrderRepository.findAllByDeadlineAtIsNotNullAndMonitoringStatusNot(MonitoringStatus.CLOSED))
            .thenReturn(listOf(orderToReview))

        medicalOrderMonitoringService.reviewOrdersDeadline()

        assertThat(orderToReview.monitoringStatus).isEqualTo(MonitoringStatus.WARNING)
        assertThat(orderToReview.updatedAt).isEqualTo(now)
        //not changed
        assertThat(orderToReview.orderId).isEqualTo(orderId)
        assertThat(orderToReview.status).isEqualTo(status)
        assertThat(orderToReview.deadlineAt).isEqualTo(deadlineAt)
        assertThat(orderToReview.statusChangedAt).isEqualTo(statusChangedAt)

        verify(medicalOrderRepository).findAllByDeadlineAtIsNotNullAndMonitoringStatusNot(MonitoringStatus.CLOSED)
    }

    @Test
    fun shouldSetOverdueWhenDeadlineHasPassed(){
        val orderId =1L
        val status = OrderStatus.NEW
        val deadlineAt = now.minusHours(1)
        val updatedAt = deadlineAt.minusDays(1)
        val statusChangedAt = updatedAt
        val monitoringStatus = MonitoringStatus.ON_TIME
        val orderToReview = MonitoredOrder(orderId, status, statusChangedAt, deadlineAt, monitoringStatus, updatedAt)

        whenever(medicalOrderRepository.findAllByDeadlineAtIsNotNullAndMonitoringStatusNot(MonitoringStatus.CLOSED))
            .thenReturn(listOf(orderToReview))

        medicalOrderMonitoringService.reviewOrdersDeadline()

        assertThat(orderToReview.monitoringStatus).isEqualTo(MonitoringStatus.OVERDUE)
        assertThat(orderToReview.updatedAt).isEqualTo(now)
        //not changed
        assertThat(orderToReview.orderId).isEqualTo(orderId)
        assertThat(orderToReview.status).isEqualTo(status)
        assertThat(orderToReview.deadlineAt).isEqualTo(deadlineAt)
        assertThat(orderToReview.statusChangedAt).isEqualTo(statusChangedAt)

        verify(medicalOrderRepository).findAllByDeadlineAtIsNotNullAndMonitoringStatusNot(MonitoringStatus.CLOSED)
    }


    @Test
    fun shouldNotModifyRecordWhenDeadlineIsFar(){
        val orderId =1L
        val status = OrderStatus.NEW
        val deadlineAt = now.plusHours(7)
        val updatedAt = deadlineAt.minusDays(1)
        val statusChangedAt = updatedAt
        val monitoringStatus = MonitoringStatus.ON_TIME
        val orderToReview = MonitoredOrder(orderId, status, statusChangedAt, deadlineAt, monitoringStatus, updatedAt)

        whenever(medicalOrderRepository.findAllByDeadlineAtIsNotNullAndMonitoringStatusNot(MonitoringStatus.CLOSED))
            .thenReturn(listOf(orderToReview))

        medicalOrderMonitoringService.reviewOrdersDeadline()

        assertThat(orderToReview.monitoringStatus).isEqualTo(monitoringStatus)
        assertThat(orderToReview.updatedAt).isEqualTo(updatedAt)
        assertThat(orderToReview.orderId).isEqualTo(orderId)
        assertThat(orderToReview.status).isEqualTo(status)
        assertThat(orderToReview.deadlineAt).isEqualTo(deadlineAt)
        assertThat(orderToReview.statusChangedAt).isEqualTo(statusChangedAt)

        verify(medicalOrderRepository).findAllByDeadlineAtIsNotNullAndMonitoringStatusNot(MonitoringStatus.CLOSED)
    }

    @Test
    fun shouldSetWarningWhenDeadlineIsExactlyInThreshold(){
        val orderId =1L
        val status = OrderStatus.NEW
        val deadlineAt = now.plusHours(warningThresholdHours)
        val updatedAt = deadlineAt.minusDays(1)
        val statusChangedAt = updatedAt
        val monitoringStatus = MonitoringStatus.ON_TIME
        val orderToReview = MonitoredOrder(orderId, status, statusChangedAt, deadlineAt, monitoringStatus, updatedAt)

        whenever(medicalOrderRepository.findAllByDeadlineAtIsNotNullAndMonitoringStatusNot(MonitoringStatus.CLOSED))
            .thenReturn(listOf(orderToReview))

        medicalOrderMonitoringService.reviewOrdersDeadline()

        assertThat(orderToReview.monitoringStatus).isEqualTo(MonitoringStatus.WARNING)
        assertThat(orderToReview.updatedAt).isEqualTo(now)
        //not changed
        assertThat(orderToReview.orderId).isEqualTo(orderId)
        assertThat(orderToReview.status).isEqualTo(status)
        assertThat(orderToReview.deadlineAt).isEqualTo(deadlineAt)
        assertThat(orderToReview.statusChangedAt).isEqualTo(statusChangedAt)

        verify(medicalOrderRepository).findAllByDeadlineAtIsNotNullAndMonitoringStatusNot(MonitoringStatus.CLOSED)
    }

    @Test
    fun shouldSetOverdueWhenDeadlineEqualsCurrentTime(){
        val orderId =1L
        val status = OrderStatus.NEW
        val deadlineAt = now
        val updatedAt = deadlineAt.minusDays(1)
        val statusChangedAt = updatedAt
        val monitoringStatus = MonitoringStatus.WARNING
        val orderToReview = MonitoredOrder(orderId, status, statusChangedAt, deadlineAt, monitoringStatus, updatedAt)

        whenever(medicalOrderRepository.findAllByDeadlineAtIsNotNullAndMonitoringStatusNot(MonitoringStatus.CLOSED))
            .thenReturn(listOf(orderToReview))

        medicalOrderMonitoringService.reviewOrdersDeadline()

        assertThat(orderToReview.monitoringStatus).isEqualTo(MonitoringStatus.OVERDUE)
        assertThat(orderToReview.updatedAt).isEqualTo(now)
        //not changed
        assertThat(orderToReview.orderId).isEqualTo(orderId)
        assertThat(orderToReview.status).isEqualTo(status)
        assertThat(orderToReview.deadlineAt).isEqualTo(deadlineAt)
        assertThat(orderToReview.statusChangedAt).isEqualTo(statusChangedAt)

        verify(medicalOrderRepository).findAllByDeadlineAtIsNotNullAndMonitoringStatusNot(MonitoringStatus.CLOSED)
    }
}