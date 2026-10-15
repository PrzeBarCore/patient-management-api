package com.przebarcore.notificationservice.entity

import com.przebarcore.notificationservice.global.enums.NotificationStatus
import com.przebarcore.notificationservice.global.enums.NotificationType
import com.przebarcore.notificationservice.global.enums.ReferenceType
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "notification")
class Notification(
    @Id
    val requestId : UUID,
    @Enumerated(EnumType.STRING)
    val referenceType : ReferenceType,
    val referenceId : Long,
    @Enumerated(EnumType.STRING)
    val notificationType : NotificationType,
    @Enumerated(EnumType.STRING)
    var status : NotificationStatus,
    val createdAt : LocalDateTime,
    var processedAt : LocalDateTime?,
    var attemptCount : Int,
    var nextAttemptAt : LocalDateTime?,
    var lastError : String?
)