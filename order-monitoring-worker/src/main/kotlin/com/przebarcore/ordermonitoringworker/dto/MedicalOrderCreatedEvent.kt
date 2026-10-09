package com.przebarcore.ordermonitoringworker.dto

import com.przebarcore.ordermonitoringworker.global.enums.OrderStatus
import java.time.LocalDateTime

data class MedicalOrderCreatedEvent (val orderId: Long,
                                     val status: OrderStatus,
                                     val createdAt: LocalDateTime
)