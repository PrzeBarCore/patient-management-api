package com.przebarcore.ordermonitoringworker.dto.incoming

import com.przebarcore.ordermonitoringworker.global.enums.incoming.AggregateType
import com.przebarcore.ordermonitoringworker.global.enums.incoming.EventType
import tools.jackson.databind.JsonNode
import java.time.LocalDateTime

data class RetrievedEvent (val eventId: Long, val eventType: EventType, val aggregateType: AggregateType, val aggregateId : Long, val createdAt: LocalDateTime, val payload : JsonNode)