package com.przebarcore.ordermonitoringworker.repository.projection

import com.przebarcore.ordermonitoringworker.global.enums.outgoing.OutboxAggregateType

interface OutboxAggregateGroup {
    val aggregateId: Long
    val aggregateType: OutboxAggregateType
}