package com.przebarcore.laboratoryapi.mapper;

import com.przebarcore.laboratoryapi.dto.request.CreatePatientRequest;
import com.przebarcore.laboratoryapi.dto.response.PatientResponse;
import com.przebarcore.laboratoryapi.entity.Patient;



public final class PatientMapper {
    public static Patient toEntity(CreatePatientRequest request){
        Patient patient = new Patient();
        patient.setFirstName(request.firstName());
        patient.setLastName(request.lastName());
        patient.setPesel(request.pesel());
        patient.setBirthDate(request.birthDate());
        return patient;
    }

    public static PatientResponse  toResponse(Patient patient){
        return new PatientResponse(patient.getId(), patient.getFirstName(), patient.getLastName(), patient.getBirthDate());
    }
}
