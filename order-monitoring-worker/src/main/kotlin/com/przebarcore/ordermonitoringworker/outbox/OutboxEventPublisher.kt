package com.przebarcore.ordermonitoringworker.outbox

import com.przebarcore.ordermonitoringworker.repository.OutboxEventRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class OutboxEventPublisher(private val outboxEventRepository: OutboxEventRepository, private val eventProcessor: OutboxEventProcessor) {
    companion object{
        private val logger = LoggerFactory.getLogger(OutboxEventPublisher::class.java)
    }

    fun publishAllEvents() {
        val groupedEventsToPublish = outboxEventRepository.findPendingAggregateGroups()
        for(aggregateGroup in groupedEventsToPublish) {
            try {
                eventProcessor.processEventsForAggregateGroup(aggregateGroup.aggregateId, aggregateGroup.aggregateType)
            }catch (exception: RuntimeException){
                logger.error("Error when processing events for aggregate: {} of type: {} {}", aggregateGroup.aggregateId, aggregateGroup.aggregateType, exception)
            }
        }
    }
}