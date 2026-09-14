package com.PrzeBarCore.Laboratorymanagementsystem.dto.events;

import com.PrzeBarCore.Laboratorymanagementsystem.global.OrderStatus;

import java.time.LocalDateTime;

public record MedicalOrderStatusChangedEvent(Long orderId, OrderStatus oldStatus, OrderStatus newStatus, LocalDateTime changedAt) {
}
