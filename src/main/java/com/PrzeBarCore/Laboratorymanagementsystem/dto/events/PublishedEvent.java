package com.PrzeBarCore.Laboratorymanagementsystem.dto.events;

import com.PrzeBarCore.Laboratorymanagementsystem.global.AggregateType;
import com.PrzeBarCore.Laboratorymanagementsystem.global.EventType;
import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;

public record PublishedEvent(Long eventId, EventType eventType, AggregateType aggregateType, Long aggregateId, LocalDateTime createdAt, JsonNode payload) {
}
