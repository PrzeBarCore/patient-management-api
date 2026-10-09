package com.przebarcore.laboratoryapi.dto.request;

import com.przebarcore.laboratoryapi.global.OrderStatus;
import jakarta.validation.constraints.NotNull;

import static com.przebarcore.laboratoryapi.dto.validation.ValidationMessages.ORDER_STATUS_MUST_NOT_BE_NULL;

public record UpdateMedicalOrderStatusRequest(@NotNull(message = ORDER_STATUS_MUST_NOT_BE_NULL) OrderStatus newStatus) {
}
