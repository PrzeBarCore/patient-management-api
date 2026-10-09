package com.przebarcore.laboratoryapi.repository;

import com.przebarcore.laboratoryapi.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository <Patient, Long> {
    public boolean existsByPesel(String pesel);
    public Optional<Patient> findByPesel(String pesel);
}
