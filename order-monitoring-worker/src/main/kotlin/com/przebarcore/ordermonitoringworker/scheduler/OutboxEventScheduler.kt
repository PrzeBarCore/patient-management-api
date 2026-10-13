package com.przebarcore.ordermonitoringworker.scheduler

import com.przebarcore.ordermonitoringworker.outbox.OutboxEventPublisher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class OutboxEventScheduler(val eventPublisher: OutboxEventPublisher) {
    @Scheduled(fixedDelay = 5000L)
    fun publishOutboxEvents(){
        eventPublisher.publishAllEvents()
    }
}