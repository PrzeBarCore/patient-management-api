package com.PrzeBarCore.Laboratorymanagementsystem.repository;

import com.PrzeBarCore.Laboratorymanagementsystem.entity.MedicalOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicalOrderRepository extends JpaRepository<MedicalOrder, Long> {
}
