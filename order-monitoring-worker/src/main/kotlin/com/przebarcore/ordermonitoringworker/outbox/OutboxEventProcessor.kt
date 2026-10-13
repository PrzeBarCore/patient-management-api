package com.przebarcore.ordermonitoringworker.outbox

import com.przebarcore.ordermonitoringworker.global.enums.outgoing.OutboxAggregateType
import com.przebarcore.ordermonitoringworker.outbox.sender.EventSender
import com.przebarcore.ordermonitoringworker.repository.OutboxEventRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.LocalDateTime
import kotlin.collections.forEach

@Service
class OutboxEventProcessor(
    private val outboxEventRepository: OutboxEventRepository,
    private val eventSender: EventSender,
    private val clock: Clock
) {
    @Transactional
    fun processEventsForAggregateGroup(aggregateId: Long, aggregateType: OutboxAggregateType){
        outboxEventRepository.findEventsForProcessing(aggregateId, aggregateType)
            .forEach {  event ->
            eventSender.send(event)
            event.processedAt = LocalDateTime.now(clock)}
    }
}