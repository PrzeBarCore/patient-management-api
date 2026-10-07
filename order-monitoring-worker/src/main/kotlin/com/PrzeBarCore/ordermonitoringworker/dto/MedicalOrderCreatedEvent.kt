package com.PrzeBarCore.ordermonitoringworker.dto

import com.PrzeBarCore.ordermonitoringworker.global.enums.OrderStatus
import java.time.LocalDateTime

data class MedicalOrderCreatedEvent (val orderId: Long,
                                     val status: OrderStatus,
                                     val createdAt: LocalDateTime
)