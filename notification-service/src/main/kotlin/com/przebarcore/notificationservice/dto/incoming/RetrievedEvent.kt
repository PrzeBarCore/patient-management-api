package com.przebarcore.notificationservice.dto.incoming

import com.przebarcore.notificationservice.global.enums.InboxAggregateType
import com.przebarcore.notificationservice.global.enums.InboxEventType
import tools.jackson.databind.JsonNode
import java.time.LocalDateTime

data class RetrievedEvent(val eventId: Long, val eventType: InboxEventType, val aggregateType: InboxAggregateType, val aggregateId : Long, val createdAt: LocalDateTime, val payload : JsonNode)
