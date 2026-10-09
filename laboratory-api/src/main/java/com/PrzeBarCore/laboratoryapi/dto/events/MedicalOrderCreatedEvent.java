package com.przebarcore.laboratoryapi.dto.events;

import com.przebarcore.laboratoryapi.global.OrderStatus;

import java.time.LocalDateTime;

public record MedicalOrderCreatedEvent(Long orderId, OrderStatus status, LocalDateTime createdAt) {
}
