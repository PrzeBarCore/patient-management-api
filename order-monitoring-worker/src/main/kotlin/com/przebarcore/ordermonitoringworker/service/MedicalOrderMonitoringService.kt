package com.przebarcore.ordermonitoringworker.service

import com.przebarcore.ordermonitoringworker.dto.MedicalOrderCreatedEvent
import com.przebarcore.ordermonitoringworker.dto.MedicalOrderStatusChangedEvent
import com.przebarcore.ordermonitoringworker.entity.MonitoredOrder
import com.przebarcore.ordermonitoringworker.global.enums.MonitoringStatus
import com.przebarcore.ordermonitoringworker.global.enums.OrderStatus
import com.przebarcore.ordermonitoringworker.repository.MonitoredOrderRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.LocalDateTime

@Service
class MedicalOrderMonitoringService(
    private val monitoredOrderRepository: MonitoredOrderRepository,
    @Value("\${monitoring.warning-threshold-hours}")
    private val warningThresholdHours : Long,
    private val clock: Clock
) {

    @Transactional
    fun processOrderStatusChanged(medicalOrderStatusChangedEvent : MedicalOrderStatusChangedEvent){
        val existingOrder = monitoredOrderRepository.findById(medicalOrderStatusChangedEvent.orderId)

        if(existingOrder.isPresent){
            val order = existingOrder.get()
            if (!medicalOrderStatusChangedEvent.changedAt.isAfter(order.statusChangedAt)) {
                return
            }
            order.status = medicalOrderStatusChangedEvent.newStatus
            order.statusChangedAt = medicalOrderStatusChangedEvent.changedAt
            order.deadlineAt = calculateDeadline(medicalOrderStatusChangedEvent.newStatus, medicalOrderStatusChangedEvent.changedAt)
            order.monitoringStatus = calculateMonitoringStatus(medicalOrderStatusChangedEvent.newStatus)
            order.updatedAt = LocalDateTime.now(clock)
        } else {
            val order = MonitoredOrder(
                medicalOrderStatusChangedEvent.orderId,
                medicalOrderStatusChangedEvent.newStatus,
                medicalOrderStatusChangedEvent.changedAt,
                calculateDeadline(medicalOrderStatusChangedEvent.newStatus, medicalOrderStatusChangedEvent.changedAt),
                calculateMonitoringStatus(medicalOrderStatusChangedEvent.newStatus),
                LocalDateTime.now(clock))
            monitoredOrderRepository.save(order)
        }
    }

    @Transactional
    fun processOrderCreated(event: MedicalOrderCreatedEvent) {
        val existingOrder = monitoredOrderRepository.findById(event.orderId)
        if (existingOrder.isPresent) {
            return
        }

        val order = MonitoredOrder(
            event.orderId,
            event.status,
            event.createdAt,
            calculateDeadline(event.status, event.createdAt),
            calculateMonitoringStatus(event.status),
            LocalDateTime.now(clock)
        )
        monitoredOrderRepository.save(order)
    }

    @Transactional
    fun reviewOrdersDeadline() {
        val monitoredOrdersToReview = monitoredOrderRepository.findAllByDeadlineAtIsNotNullAndMonitoringStatusNot(MonitoringStatus.CLOSED)
        val now = LocalDateTime.now(clock)
        for(monitoredOrder in monitoredOrdersToReview){
            val deadLineAt = monitoredOrder.deadlineAt
            val newMonitoringStatus = when {
                !now.isBefore(deadLineAt) -> MonitoringStatus.OVERDUE
                !now.plusHours(warningThresholdHours).isBefore(deadLineAt) -> MonitoringStatus.WARNING
                else -> MonitoringStatus.ON_TIME
            }

            if(newMonitoringStatus != monitoredOrder.monitoringStatus){
                monitoredOrder.monitoringStatus = newMonitoringStatus
                monitoredOrder.updatedAt = now
            }
        }
    }

    private fun calculateDeadline(orderStatus : OrderStatus, changedAt : LocalDateTime) : LocalDateTime? {
        return when (orderStatus) {
            OrderStatus.NEW -> changedAt.plusDays(1L)
            OrderStatus.IN_PROCESS -> changedAt.plusDays(2L)
            OrderStatus.COMPLETED, OrderStatus.CANCELED -> null
        }
    }
    private fun calculateMonitoringStatus(orderStatus : OrderStatus) : MonitoringStatus {
        return when (orderStatus) {
            OrderStatus.NEW, OrderStatus.IN_PROCESS  -> MonitoringStatus.ON_TIME
            OrderStatus.COMPLETED, OrderStatus.CANCELED -> MonitoringStatus.CLOSED
        }
    }


}