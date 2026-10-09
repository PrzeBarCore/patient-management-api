package com.przebarcore.ordermonitoringworker.entity

import com.przebarcore.ordermonitoringworker.global.enums.EventStatus
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "processed_event")
class ProcessedEvent(
    @Id
    val eventId: Long,
    var processedAt: LocalDateTime?,
    var startedAt: LocalDateTime,
    @Enumerated(EnumType.STRING)
    var status: EventStatus
)