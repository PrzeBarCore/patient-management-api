package com.przebarcore.laboratoryapi.dto.events;

import com.przebarcore.laboratoryapi.global.OrderStatus;

import java.time.LocalDateTime;

public record MedicalOrderStatusChangedEvent(Long orderId, OrderStatus oldStatus, OrderStatus newStatus, LocalDateTime changedAt) {
}
