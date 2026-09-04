package com.PrzeBarCore.Laboratorymanagementsystem.service.impl;

import com.PrzeBarCore.Laboratorymanagementsystem.dto.request.CreatePatientRequest;
import com.PrzeBarCore.Laboratorymanagementsystem.dto.request.UpdatePatientRequest;
import com.PrzeBarCore.Laboratorymanagementsystem.dto.response.PatientResponse;
import com.PrzeBarCore.Laboratorymanagementsystem.entity.Patient;
import com.PrzeBarCore.Laboratorymanagementsystem.exception.PatientAlreadyExistsException;
import com.PrzeBarCore.Laboratorymanagementsystem.exception.PatientNotFoundException;
import com.PrzeBarCore.Laboratorymanagementsystem.mapper.PatientMapper;
import com.PrzeBarCore.Laboratorymanagementsystem.repository.PatientRepository;
import com.PrzeBarCore.Laboratorymanagementsystem.service.PatientService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PatientServiceImpl implements PatientService {
    private final PatientRepository repository;

    public PatientServiceImpl(PatientRepository repository){
        this.repository = repository;
    }

    @Transactional
    @Override
    public PatientResponse create(CreatePatientRequest request) {
        if(repository.existsByPesel(request.pesel()))
            throw new PatientAlreadyExistsException(request.pesel());

        return PatientMapper.toResponse(repository.save(PatientMapper.toEntity(request)));
    }

    @Transactional(readOnly = true)
    @Override
    public List<PatientResponse> findAll() {
        return repository.findAll().stream().map(PatientMapper::toResponse).toList();
    }
    @Transactional(readOnly = true)
    @Override
    public PatientResponse findById(Long id) {
        return repository.findById(id).map(PatientMapper::toResponse).orElseThrow(() -> new PatientNotFoundException(id));
    }

    @Transactional
    @Override
    public PatientResponse update(Long id, UpdatePatientRequest request) {
        Patient patient = repository.findById(id).orElseThrow(() -> new PatientNotFoundException(id));
        patient.setFirstName(request.firstName());
        patient.setLastName(request.lastName());
        patient.setBirthDate(request.birthDate());
        return PatientMapper.toResponse(patient);
    }
    @Transactional
    @Override
    public void delete(Long id) {
        Patient patient = repository.findById(id).orElseThrow(()->new PatientNotFoundException(id));
        repository.delete(patient);
    }
}
