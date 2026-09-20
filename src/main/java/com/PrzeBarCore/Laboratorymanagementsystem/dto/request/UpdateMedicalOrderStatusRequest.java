package com.PrzeBarCore.Laboratorymanagementsystem.dto.request;

import com.PrzeBarCore.Laboratorymanagementsystem.global.OrderStatus;
import jakarta.validation.constraints.NotNull;

import static com.PrzeBarCore.Laboratorymanagementsystem.dto.validation.ValidationMessages.ORDER_STATUS_MUST_NOT_BE_NULL;

public record UpdateMedicalOrderStatusRequest(@NotNull(message = ORDER_STATUS_MUST_NOT_BE_NULL) OrderStatus newStatus) {
}
