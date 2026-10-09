package com.przebarcore.laboratoryapi.service;

import com.przebarcore.laboratoryapi.dto.request.CreateMedicalOrderRequest;
import com.przebarcore.laboratoryapi.dto.response.MedicalOrderResponse;
import com.przebarcore.laboratoryapi.global.OrderStatus;

import java.util.List;

public interface MedicalOrderService {
    MedicalOrderResponse create(CreateMedicalOrderRequest medicalOrder);
    MedicalOrderResponse updateStatus(Long id, OrderStatus status);
    List<MedicalOrderResponse> findAll();

    MedicalOrderResponse findById(Long id);

    void delete(Long id);
}
