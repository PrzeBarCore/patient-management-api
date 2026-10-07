package com.PrzeBarCore.ordermonitoringworker.service

import com.PrzeBarCore.ordermonitoringworker.dto.MedicalOrderCreatedEvent
import com.PrzeBarCore.ordermonitoringworker.dto.MedicalOrderStatusChangedEvent
import com.PrzeBarCore.ordermonitoringworker.entity.MonitoredOrder
import com.PrzeBarCore.ordermonitoringworker.global.enums.MonitoringStatus
import com.PrzeBarCore.ordermonitoringworker.global.enums.OrderStatus
import com.PrzeBarCore.ordermonitoringworker.repository.MonitoredOrderRepository
import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class MedicalOrderMonitoringService(private val monitoredOrderRepository: MonitoredOrderRepository) {

    @Transactional
    fun processOrderStatusChanged(medicalOrderStatusChangedEvent : MedicalOrderStatusChangedEvent){
        val existingOrder = monitoredOrderRepository.findById(medicalOrderStatusChangedEvent.orderId)

        if(existingOrder.isPresent){
            val order = existingOrder.get()
            order.status = medicalOrderStatusChangedEvent.newStatus
            order.statusChangedAt = medicalOrderStatusChangedEvent.changedAt
            order.deadlineAt = calculateDeadline(medicalOrderStatusChangedEvent.newStatus, medicalOrderStatusChangedEvent.changedAt)
            order.monitoringStatus = calculateMonitoringStatus(medicalOrderStatusChangedEvent.newStatus)
            order.updatedAt = LocalDateTime.now()
        } else {
            val order = MonitoredOrder(
                medicalOrderStatusChangedEvent.orderId,
                medicalOrderStatusChangedEvent.newStatus,
                medicalOrderStatusChangedEvent.changedAt,
                calculateDeadline(medicalOrderStatusChangedEvent.newStatus, medicalOrderStatusChangedEvent.changedAt),
                calculateMonitoringStatus(medicalOrderStatusChangedEvent.newStatus),
                LocalDateTime.now())
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
            LocalDateTime.now()
        )
        monitoredOrderRepository.save(order)
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