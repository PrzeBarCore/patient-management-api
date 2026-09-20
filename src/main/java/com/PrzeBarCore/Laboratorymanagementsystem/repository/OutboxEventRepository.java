package com.PrzeBarCore.Laboratorymanagementsystem.repository;

import com.PrzeBarCore.Laboratorymanagementsystem.entity.OutboxEvent;
import com.PrzeBarCore.Laboratorymanagementsystem.global.AggregateType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    List<OutboxEvent> findAllByProcessedAtIsNull();
    @Query(value = "SELECT event FROM OutboxEvent event WHERE event.processedAt IS NULL AND event.aggregateId = ?1 AND event.aggregateType = ?2 ORDER BY event.createdAt, event.id ASC")
    List<OutboxEvent> findEventsForProcessing(Long aggregateId, AggregateType aggregateType);

}
