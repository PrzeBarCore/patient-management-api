package com.przebarcore.ordermonitoringworker.processing

import com.przebarcore.ordermonitoringworker.dto.PublishedEvent
import com.przebarcore.ordermonitoringworker.global.enums.EventStatus
import com.przebarcore.ordermonitoringworker.repository.ProcessedEventRepository
import com.przebarcore.ordermonitoringworker.routing.EventRouter
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class EventProcessor(
    private val eventRepository: ProcessedEventRepository,
    private val eventRouter: EventRouter){

    @Transactional
    fun process(event: PublishedEvent) {
        val claimed = eventRepository.tryClaimEvent(
            event.eventId,
            LocalDateTime.now()
        )

        if(claimed == 0){
            return
        }

        eventRouter.route(event)
        val processedEvent = eventRepository.findById(event.eventId).orElseThrow()
        processedEvent.processedAt = LocalDateTime.now()
        processedEvent.status = EventStatus.PROCESSED

    }
}