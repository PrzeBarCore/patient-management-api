package com.przebarcore.ordermonitoringworker.repository

import com.przebarcore.ordermonitoringworker.entity.OutboxEvent
import com.przebarcore.ordermonitoringworker.global.enums.outgoing.OutboxAggregateType
import com.przebarcore.ordermonitoringworker.repository.projection.OutboxAggregateGroup
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface OutboxEventRepository : JpaRepository<OutboxEvent, Long> {
    @Query(value = """SELECT DISTINCT event.aggregateId AS aggregateId, event.aggregateType AS aggregateType 
        FROM OutboxEvent event 
        WHERE event.processedAt IS NULL""")
    fun findPendingAggregateGroups() : List<OutboxAggregateGroup>
    @Query(value = """SELECT event 
        FROM OutboxEvent event
        WHERE event.processedAt IS NULL AND event.aggregateId = ?1 AND event.aggregateType = ?2
        ORDER BY event.createdAt ASC, event.id ASC""")
     fun findEventsForProcessing(aggregateId: Long, aggregateType: OutboxAggregateType) : List<OutboxEvent>
}