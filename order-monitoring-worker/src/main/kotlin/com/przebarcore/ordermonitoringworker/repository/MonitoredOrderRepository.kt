package com.przebarcore.ordermonitoringworker.repository

import com.przebarcore.ordermonitoringworker.entity.MonitoredOrder
import com.przebarcore.ordermonitoringworker.global.enums.MonitoringStatus
import org.springframework.data.jpa.repository.JpaRepository

interface MonitoredOrderRepository : JpaRepository<MonitoredOrder, Long> {
    fun findAllByDeadlineAtIsNotNullAndMonitoringStatusNot(monitoringStatus: MonitoringStatus): List<MonitoredOrder>
}