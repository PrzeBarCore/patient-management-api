package com.przebarcore.ordermonitoringworker.dto

import com.przebarcore.ordermonitoringworker.global.enums.OrderStatus
import java.time.LocalDateTime

data class MedicalOrderStatusChangedEvent(val orderId: Long,
                                          val oldStatus: OrderStatus,
                                          val newStatus: OrderStatus,
                                          val changedAt: LocalDateTime
)
