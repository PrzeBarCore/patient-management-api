package com.przebarcore.laboratoryapi.repository;

import com.przebarcore.laboratoryapi.entity.MedicalOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicalOrderRepository extends JpaRepository<MedicalOrder, Long> {
}
