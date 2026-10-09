package com.przebarcore.ordermonitoringworker.routing

import com.przebarcore.ordermonitoringworker.dto.PublishedEvent
import com.przebarcore.ordermonitoringworker.global.enums.EventType
import com.przebarcore.ordermonitoringworker.handler.EventHandler
import org.springframework.stereotype.Component

@Component
class EventRouter( handlers : List<EventHandler>) {

    private val handlersByType: Map<EventType, EventHandler> =
        handlers.associateBy { it.eventType }

    fun route(event: PublishedEvent){
        val handler = handlersByType[event.eventType] ?:
        throw IllegalArgumentException("No handler for event type ${event.eventType}")

        handler.handle(event)
    }

}