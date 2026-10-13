package com.przebarcore.ordermonitoringworker.exception

import com.przebarcore.ordermonitoringworker.global.enums.outgoing.OutboxAggregateType

class EventSerializationException(aggregateType: OutboxAggregateType, aggregateId: Long, cause: Throwable)
    : RuntimeException("Serialization exception for: " + aggregateType.name + " of id " + aggregateId, cause) {
}