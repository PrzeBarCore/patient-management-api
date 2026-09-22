package com.PrzeBarCore.ordermonitoringworker.dto

import com.PrzeBarCore.ordermonitoringworker.global.enums.OrderStatus
import java.time.LocalDateTime

data class MedicalOrderStatusChangedEvent(val orderId: Long,
                                          val oldStatus: OrderStatus,
                                          val newStatus: OrderStatus,
                                          val changedAt: LocalDateTime
)
