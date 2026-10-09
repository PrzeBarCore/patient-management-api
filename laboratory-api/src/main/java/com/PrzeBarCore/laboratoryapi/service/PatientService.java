package com.przebarcore.laboratoryapi.service;

import com.przebarcore.laboratoryapi.dto.request.CreatePatientRequest;
import com.przebarcore.laboratoryapi.dto.request.UpdatePatientRequest;
import com.przebarcore.laboratoryapi.dto.response.PatientResponse;

import java.util.List;

public interface PatientService {
    PatientResponse create(CreatePatientRequest patient);
    List<PatientResponse> findAll();

    PatientResponse findById(Long id);

    PatientResponse update(Long id, UpdatePatientRequest request);

    void delete(Long id);
}
