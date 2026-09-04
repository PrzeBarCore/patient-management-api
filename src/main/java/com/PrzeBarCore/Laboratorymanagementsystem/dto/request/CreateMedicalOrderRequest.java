package com.PrzeBarCore.Laboratorymanagementsystem.dto.request;
import jakarta.validation.constraints.NotNull;

public record CreateMedicalOrderRequest (@NotNull(message = "Patient Id cannot be null") Long patientId){
}
