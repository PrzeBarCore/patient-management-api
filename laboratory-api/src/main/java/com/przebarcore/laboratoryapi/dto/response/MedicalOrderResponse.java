package com.przebarcore.laboratoryapi.dto.response;

import com.przebarcore.laboratoryapi.global.OrderStatus;

import java.time.LocalDateTime;

public record MedicalOrderResponse (Long id,LocalDateTime registrationDateTime,OrderStatus status, Long patientId){
}
