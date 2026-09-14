package com.PrzeBarCore.Laboratorymanagementsystem.repository;

import com.PrzeBarCore.Laboratorymanagementsystem.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    List<OutboxEvent> findAllByProcessedAtIsNull();

}
