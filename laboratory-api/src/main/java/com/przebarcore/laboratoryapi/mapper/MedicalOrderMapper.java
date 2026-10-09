package com.przebarcore.laboratoryapi.mapper;

import com.przebarcore.laboratoryapi.dto.response.MedicalOrderResponse;
import com.przebarcore.laboratoryapi.entity.MedicalOrder;


public final class MedicalOrderMapper {
     private MedicalOrderMapper(){};
    public static MedicalOrderResponse toResponse(MedicalOrder order){
        return new MedicalOrderResponse(order.getId(), order.getRegistrationDateTime(), order.getStatus(), order.getPatient().getId());
    }
}
