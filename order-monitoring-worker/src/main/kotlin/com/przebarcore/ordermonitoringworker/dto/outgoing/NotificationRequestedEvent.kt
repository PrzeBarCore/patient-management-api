package com.przebarcore.ordermonitoringworker.dto.outgoing

import com.przebarcore.ordermonitoringworker.global.enums.outgoing.NotificationType
import com.przebarcore.ordermonitoringworker.global.enums.outgoing.ReferenceType
import java.time.LocalDateTime
import java.util.UUID

data class NotificationRequestedEvent(
    val requestId: UUID,
    val referenceType: ReferenceType,
    val referenceId: Long,
    val notificationType: NotificationType,
    val createdAt: LocalDateTime)
