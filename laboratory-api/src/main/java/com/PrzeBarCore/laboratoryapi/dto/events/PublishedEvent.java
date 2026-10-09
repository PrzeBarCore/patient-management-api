package com.przebarcore.laboratoryapi.dto.events;

import com.przebarcore.laboratoryapi.global.AggregateType;
import com.przebarcore.laboratoryapi.global.EventType;
import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;

public record PublishedEvent(Long eventId, EventType eventType, AggregateType aggregateType, Long aggregateId, LocalDateTime createdAt, JsonNode payload) {
}
