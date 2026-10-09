package com.przebarcore.ordermonitoringworker.processing

import com.przebarcore.ordermonitoringworker.dto.PublishedEvent
import com.przebarcore.ordermonitoringworker.entity.ProcessedEvent
import com.przebarcore.ordermonitoringworker.global.enums.AggregateType
import com.przebarcore.ordermonitoringworker.global.enums.EventStatus
import com.przebarcore.ordermonitoringworker.global.enums.EventType
import com.przebarcore.ordermonitoringworker.repository.ProcessedEventRepository
import com.przebarcore.ordermonitoringworker.routing.EventRouter
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import tools.jackson.databind.node.JsonNodeFactory
import java.time.LocalDateTime
import java.util.Optional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows

import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class EventProcessorTest {
    @Mock
    lateinit var eventRepository : ProcessedEventRepository
    @Mock
    lateinit var eventRouter: EventRouter
    @InjectMocks
    lateinit var eventProcessor: EventProcessor

    @Test
    fun shouldProcessNewEvent(){
        val publishedEvent = PublishedEvent(1L,
            EventType.MEDICAL_ORDER_STATUS_CHANGED,
            AggregateType.MEDICAL_ORDER,
            1L,
            LocalDateTime.now(),
            JsonNodeFactory.instance.objectNode()
        )

        val processedEvent = ProcessedEvent(publishedEvent.eventId, null, LocalDateTime.now(), EventStatus.PROCESSING)

        whenever(eventRepository.tryClaimEvent( eq(publishedEvent.eventId), any())).thenReturn(1)
        whenever(eventRepository.findById(publishedEvent.eventId)).thenReturn(Optional.of(processedEvent))

        eventProcessor.process(publishedEvent)

        assertThat(processedEvent.status)
            .isEqualTo(EventStatus.PROCESSED)

        assertThat(processedEvent.processedAt)
            .isNotNull()
        verify(eventRepository).tryClaimEvent( eq(publishedEvent.eventId), any())
        verify(eventRepository).findById(publishedEvent.eventId)
        verify(eventRouter).route(publishedEvent)
    }

    @Test
    fun shouldSkipAlreadyProcessedEvent(){
        val publishedEvent = PublishedEvent(1L,
            EventType.MEDICAL_ORDER_STATUS_CHANGED,
            AggregateType.MEDICAL_ORDER,
            1L,
            LocalDateTime.now(),
            JsonNodeFactory.instance.objectNode()
        )

        whenever(eventRepository.tryClaimEvent( eq(publishedEvent.eventId), any())).thenReturn(0)

        eventProcessor.process(publishedEvent)

        verify(eventRepository).tryClaimEvent( eq(publishedEvent.eventId), any())
        verifyNoInteractions(eventRouter)
        verify(eventRepository, never()).findById(publishedEvent.eventId)
    }

    @Test
    fun shouldNotMarkEventAsProcessedWhenHandlerFails(){
        val publishedEvent = PublishedEvent(1L,
            EventType.MEDICAL_ORDER_STATUS_CHANGED,
            AggregateType.MEDICAL_ORDER,
            1L,
            LocalDateTime.now(),
            JsonNodeFactory.instance.objectNode()
        )

        whenever(eventRepository.tryClaimEvent( eq(publishedEvent.eventId), any())).thenReturn(1)
        doThrow(RuntimeException("Handler failed"))
            .whenever(eventRouter)
            .route(publishedEvent)

        assertThrows(RuntimeException::class.java) {
            eventProcessor.process(publishedEvent)
        }

        verify(eventRepository).tryClaimEvent( eq(publishedEvent.eventId), any())
        verify(eventRouter).route(publishedEvent)
        verify(eventRepository, never()).findById(publishedEvent.eventId)
    }

}