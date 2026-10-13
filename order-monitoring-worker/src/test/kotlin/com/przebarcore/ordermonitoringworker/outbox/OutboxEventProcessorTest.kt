package com.przebarcore.ordermonitoringworker.outbox

import com.przebarcore.ordermonitoringworker.entity.OutboxEvent
import com.przebarcore.ordermonitoringworker.global.enums.outgoing.OutboxAggregateType
import com.przebarcore.ordermonitoringworker.global.enums.outgoing.OutboxEventType
import com.przebarcore.ordermonitoringworker.outbox.sender.EventSender
import com.przebarcore.ordermonitoringworker.repository.OutboxEventRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@ExtendWith(MockitoExtension::class)
class OutboxEventProcessorTest {
    @Mock
    private lateinit var outboxEventRepository: OutboxEventRepository
    @Mock
    private lateinit var eventSender: EventSender

    private lateinit var outboxEventProcessor: OutboxEventProcessor

    private val clock = Clock.fixed(Instant.parse("2026-10-11T12:12:00.00Z"), ZoneId.of("UTC"))
    private val now = LocalDateTime.now(clock)
    @BeforeEach
    fun setUp() {
        outboxEventProcessor = OutboxEventProcessor(outboxEventRepository, eventSender, clock)
    }

    @Test
    fun  shouldProcessAllEventsInOrder(){
        val outboxEvent1 = OutboxEvent(1L,
            1L ,
            OutboxAggregateType.MEDICAL_ORDER,
            OutboxEventType.NOTIFICATION_REQUESTED,
            "",
            now.minusHours(3), null)
        val outboxEvent2 = OutboxEvent(2L,
            1L ,
            OutboxAggregateType.MEDICAL_ORDER,
            OutboxEventType.NOTIFICATION_REQUESTED,
            "",
            now.minusHours(2), null)

        val outboxEvent3 = OutboxEvent(3L,
            1L ,
            OutboxAggregateType.MEDICAL_ORDER,
            OutboxEventType.NOTIFICATION_REQUESTED,
            "",
            now.minusHours(1), null)


        whenever(outboxEventRepository.findEventsForProcessing(1L, OutboxAggregateType.MEDICAL_ORDER))
            .thenReturn(listOf(outboxEvent1, outboxEvent2, outboxEvent3))

        outboxEventProcessor.processEventsForAggregateGroup(1L, OutboxAggregateType.MEDICAL_ORDER)

        val inOrder = inOrder(eventSender)
        inOrder.verify(eventSender).send(outboxEvent1)
        inOrder.verify(eventSender).send(outboxEvent2)
        inOrder.verify(eventSender).send(outboxEvent3)

        assertThat(outboxEvent1.processedAt).isEqualTo(now)
        assertThat(outboxEvent2.processedAt).isEqualTo(now)
        assertThat(outboxEvent3.processedAt).isEqualTo(now)

        verify(outboxEventRepository, never()).save(any())
        verify(outboxEventRepository).findEventsForProcessing(1L, OutboxAggregateType.MEDICAL_ORDER )
        verify(eventSender, times(3)).send(any())
    }

    @Test
    fun  shouldStopProcessingWhenExceptionIsThrown(){
        val outboxEvent1 = OutboxEvent(1L,
            1L ,
            OutboxAggregateType.MEDICAL_ORDER,
            OutboxEventType.NOTIFICATION_REQUESTED,
            "",
            now.minusHours(3), null)
        val outboxEvent2 = OutboxEvent(2L,
            1L ,
            OutboxAggregateType.MEDICAL_ORDER,
            OutboxEventType.NOTIFICATION_REQUESTED,
            "",
            now.minusHours(2), null)

        val outboxEvent3 = OutboxEvent(3L,
            1L ,
            OutboxAggregateType.MEDICAL_ORDER,
            OutboxEventType.NOTIFICATION_REQUESTED,
            "",
            now.minusHours(1), null)


        whenever(outboxEventRepository.findEventsForProcessing(1L, OutboxAggregateType.MEDICAL_ORDER))
            .thenReturn(listOf(outboxEvent1, outboxEvent2, outboxEvent3))

        doNothing().whenever(eventSender).send(outboxEvent1)
        doThrow(RuntimeException("Mam ochotę na partyjkę Gwinta")).whenever(eventSender).send(outboxEvent2)

        assertThrows<RuntimeException>{
            outboxEventProcessor.processEventsForAggregateGroup(1L, OutboxAggregateType.MEDICAL_ORDER)
        }

        val inOrder = inOrder(eventSender)
        inOrder.verify(eventSender).send(outboxEvent1)
        inOrder.verify(eventSender).send(outboxEvent2)

        verify(outboxEventRepository)
            .findEventsForProcessing(1L, OutboxAggregateType.MEDICAL_ORDER)
        verify(outboxEventRepository, never()).save(any())
        verify(eventSender).send(outboxEvent2)
        verify(eventSender, never()).send(outboxEvent3)
    }
}