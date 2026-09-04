package com.PrzeBarCore.Laboratorymanagementsystem.dto.response;

import com.PrzeBarCore.Laboratorymanagementsystem.entity.Patient;
import com.PrzeBarCore.Laboratorymanagementsystem.global.OrderStatus;

import java.time.LocalDateTime;

public record MedicalOrderResponse (Long id,LocalDateTime registrationDateTime,OrderStatus status, Long patientId){
}
