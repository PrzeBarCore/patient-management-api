package com.przebarcore.ordermonitoringworker.dto.outgoing

import com.przebarcore.ordermonitoringworker.global.enums.outgoing.OutboxAggregateType
import com.przebarcore.ordermonitoringworker.global.enums.outgoing.OutboxEventType
import tools.jackson.databind.JsonNode
import java.time.LocalDateTime

data class PublishedEvent (val eventId: Long, val eventType: OutboxEventType, val aggregateType: OutboxAggregateType, val aggregateId : Long, val createdAt: LocalDateTime, val payload : JsonNode)