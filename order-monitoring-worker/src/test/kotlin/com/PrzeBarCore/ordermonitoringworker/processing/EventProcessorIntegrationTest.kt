package com.PrzeBarCore.ordermonitoringworker.processing

import com.PrzeBarCore.ordermonitoringworker.dto.PublishedEvent
import com.PrzeBarCore.ordermonitoringworker.global.enums.AggregateType
import com.PrzeBarCore.ordermonitoringworker.global.enums.EventStatus
import com.PrzeBarCore.ordermonitoringworker.global.enums.EventType
import com.PrzeBarCore.ordermonitoringworker.repository.ProcessedEventRepository
import com.PrzeBarCore.ordermonitoringworker.routing.EventRouter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import tools.jackson.databind.node.JsonNodeFactory
import java.time.LocalDateTime
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@SpringBootTest
@Testcontainers
class EventProcessorIntegrationTest {
    companion object {
        @Container
        @ServiceConnection
        @JvmStatic
        val postgres = PostgreSQLContainer("postgres:16")
    }

    @Autowired
    lateinit var eventProcessor: EventProcessor

    @Autowired
    lateinit var eventRepository: ProcessedEventRepository

    @MockitoBean
    lateinit var eventRouter: EventRouter

    @BeforeEach
    fun clearData() {
        eventRepository.deleteAll()
    }

    @Test
    fun shouldRollbackTransactionWhenExceptionIsThrown(){
        val publishedEvent = PublishedEvent(1L,
            EventType.MEDICAL_ORDER_STATUS_CHANGED,
            AggregateType.MEDICAL_ORDER,
            1L,
            LocalDateTime.now(),
            JsonNodeFactory.instance.objectNode()
        )

        doThrow(RuntimeException("Handler failed"))
            .whenever(eventRouter)
            .route(publishedEvent)

        assertThrows(RuntimeException::class.java) {
            eventProcessor.process(publishedEvent)
        }

        assertThat(eventRepository.findById(publishedEvent.eventId)).isEmpty
        verify(eventRouter).route(publishedEvent)
    }

    @Test
    fun shouldProcessEvent(){
        val publishedEvent = PublishedEvent(1L,
            EventType.MEDICAL_ORDER_STATUS_CHANGED,
            AggregateType.MEDICAL_ORDER,
            1L,
            LocalDateTime.now(),
            JsonNodeFactory.instance.objectNode()
        )

            eventProcessor.process(publishedEvent)

        val processedEvent = eventRepository.findById(publishedEvent.eventId).orElseThrow()
        assertThat(processedEvent.status).isEqualTo(EventStatus.PROCESSED)
        assertThat(processedEvent.processedAt).isNotNull()
        assertThat(processedEvent.startedAt).isNotNull()
        verify(eventRouter).route(publishedEvent)
    }

    @Test
    fun shouldProcessSameEventOnlyOnce(){
        val publishedEvent = PublishedEvent(1L,
            EventType.MEDICAL_ORDER_STATUS_CHANGED,
            AggregateType.MEDICAL_ORDER,
            1L,
            LocalDateTime.now(),
            JsonNodeFactory.instance.objectNode()
        )

        eventProcessor.process(publishedEvent)
        eventProcessor.process(publishedEvent)

        verify(eventRouter, times(1))
            .route(publishedEvent)
        assertThat(eventRepository.count()).isEqualTo(1)
        val processedEvent = eventRepository.findById(publishedEvent.eventId).orElseThrow()
        assertThat(processedEvent.status).isEqualTo(EventStatus.PROCESSED)
        assertThat(processedEvent.processedAt).isNotNull()
    }

    @Test
    fun shouldProcessSameEventOnlyOnceWhenTwoWorkersRunConcurrently() {
        val publishedEvent = PublishedEvent(
            100L,
            EventType.MEDICAL_ORDER_STATUS_CHANGED,
            AggregateType.MEDICAL_ORDER,
            100L,
            LocalDateTime.now(),
            JsonNodeFactory.instance.objectNode()
        )

        val threadCount = 2
        val readyLatch = CountDownLatch(threadCount)
        val startLatch = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(threadCount)

        val tasks = (1..threadCount).map {
            executor.submit {
                readyLatch.countDown()
                startLatch.await()
                eventProcessor.process(publishedEvent)
            }
        }

        readyLatch.await()
        startLatch.countDown()
        tasks.forEach { it.get() }

        executor.shutdown()

        verify(eventRouter, times(1)).route(publishedEvent)
        assertThat(eventRepository.count()).isEqualTo(1)

        val processedEvent = eventRepository.findById(publishedEvent.eventId).orElseThrow()
        assertThat(processedEvent.status).isEqualTo(EventStatus.PROCESSED)
        assertThat(processedEvent.processedAt).isNotNull()
    }
}