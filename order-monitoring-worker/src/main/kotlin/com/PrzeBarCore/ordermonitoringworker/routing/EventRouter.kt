package com.PrzeBarCore.ordermonitoringworker.routing

import com.PrzeBarCore.ordermonitoringworker.dto.PublishedEvent
import com.PrzeBarCore.ordermonitoringworker.global.enums.EventType
import com.PrzeBarCore.ordermonitoringworker.handler.EventHandler
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