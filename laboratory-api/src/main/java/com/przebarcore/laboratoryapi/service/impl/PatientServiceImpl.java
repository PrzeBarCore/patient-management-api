package com.przebarcore.laboratoryapi.service.impl;

import com.przebarcore.laboratoryapi.dto.request.CreatePatientRequest;
import com.przebarcore.laboratoryapi.dto.request.UpdatePatientRequest;
import com.przebarcore.laboratoryapi.dto.response.PatientResponse;
import com.przebarcore.laboratoryapi.entity.Patient;
import com.przebarcore.laboratoryapi.exception.PatientAlreadyExistsException;
import com.przebarcore.laboratoryapi.exception.PatientNotFoundException;
import com.przebarcore.laboratoryapi.mapper.PatientMapper;
import com.przebarcore.laboratoryapi.repository.PatientRepository;
import com.przebarcore.laboratoryapi.service.PatientService;
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
