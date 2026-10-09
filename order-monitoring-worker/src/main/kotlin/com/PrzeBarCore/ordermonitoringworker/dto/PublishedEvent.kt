package com.przebarcore.ordermonitoringworker.dto

import com.przebarcore.ordermonitoringworker.global.enums.AggregateType
import com.przebarcore.ordermonitoringworker.global.enums.EventType
import tools.jackson.databind.JsonNode
import java.time.LocalDateTime

data class PublishedEvent (val eventId: Long, val eventType: EventType, val aggregateType: AggregateType, val aggregateId : Long, val createdAt: LocalDateTime, val payload : JsonNode) {
}