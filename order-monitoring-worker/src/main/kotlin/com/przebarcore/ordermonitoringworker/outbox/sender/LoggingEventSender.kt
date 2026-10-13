package com.przebarcore.ordermonitoringworker.outbox.sender

import com.przebarcore.ordermonitoringworker.entity.OutboxEvent
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("local")
class LoggingEventSender : EventSender{
    companion object{
         val log = LoggerFactory.getLogger(LoggingEventSender::class.java)
    }

    override fun send(event : OutboxEvent) {
        log.info("Publish event: {}", event.id)
    }
}