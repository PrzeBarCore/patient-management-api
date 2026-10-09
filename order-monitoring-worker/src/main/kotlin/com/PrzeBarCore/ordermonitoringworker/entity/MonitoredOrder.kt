package com.przebarcore.ordermonitoringworker.entity

import com.przebarcore.ordermonitoringworker.global.enums.MonitoringStatus
import com.przebarcore.ordermonitoringworker.global.enums.OrderStatus
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "monitored_order")
class MonitoredOrder(
    @Id
    val orderId : Long,
    @Enumerated(value = EnumType.STRING)
    var status : OrderStatus,
    var statusChangedAt: LocalDateTime,
    var deadlineAt: LocalDateTime?,
    @Enumerated(value = EnumType.STRING)
    var monitoringStatus: MonitoringStatus,
    var updatedAt: LocalDateTime
    )
