package com.przebarcore.ordermonitoringworker.repository

import com.przebarcore.ordermonitoringworker.entity.ProcessedEvent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface ProcessedEventRepository : JpaRepository<ProcessedEvent, Long> {
    @Modifying
    @Query(
        value = """
        INSERT INTO processed_event (
            event_id,
            status,
            started_at,
            processed_at
        )
        VALUES (
            :eventId,
            'PROCESSING',
            :startedAt,
            NULL
        )
        ON CONFLICT (event_id) DO NOTHING
    """,
        nativeQuery = true
    )
    fun tryClaimEvent(
        @Param("eventId") eventId: Long,
        @Param("startedAt") startedAt: LocalDateTime
    ): Int
}