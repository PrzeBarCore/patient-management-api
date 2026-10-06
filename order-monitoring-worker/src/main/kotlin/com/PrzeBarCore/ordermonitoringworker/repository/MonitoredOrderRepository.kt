package com.PrzeBarCore.ordermonitoringworker.repository

import com.PrzeBarCore.ordermonitoringworker.entity.MonitoredOrder
import org.springframework.data.jpa.repository.JpaRepository

interface MonitoredOrderRepository : JpaRepository<MonitoredOrder, Long> {
}