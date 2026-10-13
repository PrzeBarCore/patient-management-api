package com.przebarcore.notificationservice.dto.incoming

import com.przebarcore.notificationservice.global.enums.NotificationType
import com.przebarcore.notificationservice.global.enums.ReferenceType
import java.time.LocalDateTime
import java.util.UUID

data class NotificationRequestedEvent(
    val requestId : UUID,
    val referenceType : ReferenceType,
    val referenceId : Long,
    val notificationType : NotificationType,
    val createdAt : LocalDateTime)
