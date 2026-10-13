package com.przebarcore.ordermonitoringworker.entity

import com.przebarcore.ordermonitoringworker.global.enums.outgoing.OutboxAggregateType
import com.przebarcore.ordermonitoringworker.global.enums.outgoing.OutboxEventType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "outbox_event")
class OutboxEvent(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long?,
    @Column(nullable = false)
    var aggregateId: Long,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var aggregateType: OutboxAggregateType,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var eventType: OutboxEventType,
    @Column(nullable = false)
    var payload: String,
    @Column(nullable = false)
    var createdAt: LocalDateTime,
    var processedAt: LocalDateTime?
)
