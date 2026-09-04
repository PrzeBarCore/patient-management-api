package com.PrzeBarCore.Laboratorymanagementsystem.service;

import com.PrzeBarCore.Laboratorymanagementsystem.dto.request.CreateMedicalOrderRequest;
import com.PrzeBarCore.Laboratorymanagementsystem.dto.response.MedicalOrderResponse;
import com.PrzeBarCore.Laboratorymanagementsystem.global.OrderStatus;

import java.util.List;

public interface MedicalOrderService {
    MedicalOrderResponse create(CreateMedicalOrderRequest medicalOrder);
    MedicalOrderResponse updateStatus(Long id, OrderStatus status);
    List<MedicalOrderResponse> findAll();

    MedicalOrderResponse findById(Long id);

    void delete(Long id);
}
