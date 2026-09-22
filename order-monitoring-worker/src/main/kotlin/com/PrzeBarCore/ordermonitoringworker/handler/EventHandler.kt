package com.PrzeBarCore.ordermonitoringworker.handler

import com.PrzeBarCore.ordermonitoringworker.dto.PublishedEvent
import com.PrzeBarCore.ordermonitoringworker.global.enums.EventType

interface EventHandler {
    val eventType: EventType
    fun handle(receivedEvent: PublishedEvent)
}