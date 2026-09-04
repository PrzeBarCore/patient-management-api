package com.PrzeBarCore.Laboratorymanagementsystem.mapper;

import com.PrzeBarCore.Laboratorymanagementsystem.dto.response.MedicalOrderResponse;
import com.PrzeBarCore.Laboratorymanagementsystem.entity.MedicalOrder;


public final class MedicalOrderMapper {
     private MedicalOrderMapper(){};
    public static MedicalOrderResponse toResponse(MedicalOrder order){
        return new MedicalOrderResponse(order.getId(), order.getRegistrationDateTime(), order.getStatus(), order.getPatient().getId());
    }
}
