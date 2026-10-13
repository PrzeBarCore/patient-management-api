package com.przebarcore.ordermonitoringworker.outbox.sender

import com.przebarcore.ordermonitoringworker.entity.OutboxEvent

interface EventSender {
    fun send(event: OutboxEvent)
}