package com.PrzeBarCore.Laboratorymanagementsystem.service;

import com.PrzeBarCore.Laboratorymanagementsystem.dto.request.CreatePatientRequest;
import com.PrzeBarCore.Laboratorymanagementsystem.dto.request.UpdatePatientRequest;
import com.PrzeBarCore.Laboratorymanagementsystem.dto.response.PatientResponse;

import java.util.List;

public interface PatientService {
    PatientResponse create(CreatePatientRequest patient);
    List<PatientResponse> findAll();

    PatientResponse findById(Long id);

    PatientResponse update(Long id, UpdatePatientRequest request);

    void delete(Long id);
}
