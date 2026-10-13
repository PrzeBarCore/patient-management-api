package com.przebarcore.ordermonitoringworker.outbox

import com.przebarcore.ordermonitoringworker.global.enums.outgoing.OutboxAggregateType
import com.przebarcore.ordermonitoringworker.repository.OutboxEventRepository
import com.przebarcore.ordermonitoringworker.repository.projection.OutboxAggregateGroup
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class OutboxEventPublisherTest {
    @Mock
    private lateinit var outboxEventRepository: OutboxEventRepository
    @Mock
    private lateinit var eventProcessor: OutboxEventProcessor
    @InjectMocks
    private lateinit var outboxEventPublisher: OutboxEventPublisher

    @Test
    fun shouldProcessRemainingAggregatesWhenOneAggregateFails(){
    val group1: OutboxAggregateGroup =  OutboxAggregateGroupImpl(1L, OutboxAggregateType.MEDICAL_ORDER)
    val group2: OutboxAggregateGroup =  OutboxAggregateGroupImpl(2L, OutboxAggregateType.MEDICAL_ORDER)

        whenever(outboxEventRepository.findPendingAggregateGroups()).thenReturn(listOf(group1,group2))

        doThrow(RuntimeException("Pokaż mi swoje towary"))
            .doNothing().whenever(eventProcessor).processEventsForAggregateGroup(
            1L, OutboxAggregateType.MEDICAL_ORDER
        )

        outboxEventPublisher.publishAllEvents()
        val inOrder = inOrder(eventProcessor)

        inOrder.verify(eventProcessor)
            .processEventsForAggregateGroup(1L, OutboxAggregateType.MEDICAL_ORDER)

        inOrder.verify(eventProcessor)
            .processEventsForAggregateGroup(2L, OutboxAggregateType.MEDICAL_ORDER)

        verify(outboxEventRepository).findPendingAggregateGroups()
        verify(eventProcessor).processEventsForAggregateGroup(1L, OutboxAggregateType.MEDICAL_ORDER)
        verify(eventProcessor).processEventsForAggregateGroup(2L, OutboxAggregateType.MEDICAL_ORDER)

    }

    @Test
    fun shouldProcessAllPendingAggregateGroups(){
        val group1: OutboxAggregateGroup =  OutboxAggregateGroupImpl(1L, OutboxAggregateType.MEDICAL_ORDER)
        val group2: OutboxAggregateGroup =  OutboxAggregateGroupImpl(2L, OutboxAggregateType.MEDICAL_ORDER)
        val group3: OutboxAggregateGroup =  OutboxAggregateGroupImpl(3L, OutboxAggregateType.MEDICAL_ORDER)

        whenever(outboxEventRepository.findPendingAggregateGroups()).thenReturn(listOf(group1,group2, group3))


        outboxEventPublisher.publishAllEvents()

        verify(outboxEventRepository).findPendingAggregateGroups()
        verify(eventProcessor).processEventsForAggregateGroup(1L, OutboxAggregateType.MEDICAL_ORDER)
        verify(eventProcessor).processEventsForAggregateGroup(2L, OutboxAggregateType.MEDICAL_ORDER)
        verify(eventProcessor).processEventsForAggregateGroup(3L, OutboxAggregateType.MEDICAL_ORDER)

    }

    @Test
    fun shouldSkipProcessingWhenListIsEmpty(){

        whenever(outboxEventRepository.findPendingAggregateGroups()).thenReturn(listOf())

        outboxEventPublisher.publishAllEvents()

        verify(outboxEventRepository).findPendingAggregateGroups()
        verify(eventProcessor, never()).processEventsForAggregateGroup(any(), any())

    }

    private class OutboxAggregateGroupImpl(
        override val aggregateId: Long,
        override val aggregateType: OutboxAggregateType
    ) : OutboxAggregateGroup
}