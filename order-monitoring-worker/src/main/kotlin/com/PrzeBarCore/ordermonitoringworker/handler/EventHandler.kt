package com.przebarcore.ordermonitoringworker.handler

import com.przebarcore.ordermonitoringworker.dto.PublishedEvent
import com.przebarcore.ordermonitoringworker.global.enums.EventType

interface EventHandler {
    val eventType: EventType
    fun handle(receivedEvent: PublishedEvent)
}