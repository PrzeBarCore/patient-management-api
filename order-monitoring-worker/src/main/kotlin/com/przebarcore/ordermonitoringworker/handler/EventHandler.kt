package com.przebarcore.ordermonitoringworker.handler

import com.przebarcore.ordermonitoringworker.dto.incoming.RetrievedEvent
import com.przebarcore.ordermonitoringworker.global.enums.incoming.EventType

interface EventHandler {
    val eventType: EventType
    fun handle(receivedEvent: RetrievedEvent)
}