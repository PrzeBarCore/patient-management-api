package com.PrzeBarCore.Laboratorymanagementsystem.dto.events;

import com.PrzeBarCore.Laboratorymanagementsystem.global.OrderStatus;

import java.time.LocalDateTime;

public record MedicalOrderCreatedEvent(Long orderId, OrderStatus status, LocalDateTime createdAt) {
}
