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
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId

@SpringBootTest
@Testcontainers
class OutboxEventProcessorIntegrationTest {

    companion object{
        @ServiceConnection
        @JvmStatic
        @Container
        private  val postgres = PostgreSQLContainer(DockerImageName.parse("postgres:16"))
    }

    @Autowired
    private lateinit var outboxEventProcessor: OutboxEventProcessor
    @Autowired
    private lateinit var outboxEventRepository: OutboxEventRepository
    @MockitoBean
    private lateinit var eventSender: EventSender

    private val clock = Clock.fixed(java.time.Instant.parse("2026-10-11T12:12:00.00Z"), ZoneId.of("UTC"))
    private val now = LocalDateTime.now(clock)

    @BeforeEach
    fun setUp() {
        outboxEventRepository.deleteAll()
    }

    @Test
    fun shouldRollbackChangesWhenExceptionIsThrown(){
        val outboxEvent1 = OutboxEvent(null,
            1L,
            OutboxAggregateType.MEDICAL_ORDER,
            OutboxEventType.NOTIFICATION_REQUESTED,
            "",
            now.minusHours(6),
            null)

        val outboxEvent2 = OutboxEvent(null,
            1L,
            OutboxAggregateType.MEDICAL_ORDER,
            OutboxEventType.NOTIFICATION_REQUESTED,
            "",
            now.minusHours(4),
            null)

        val outboxEvent3 = OutboxEvent(null,
            1L,
            OutboxAggregateType.MEDICAL_ORDER,
            OutboxEventType.NOTIFICATION_REQUESTED,
            "",
            now.minusHours(3),
            null)

        val savedEvent1 = outboxEventRepository.save(outboxEvent1)
        val savedEvent2 = outboxEventRepository.save(outboxEvent2)
        val savedEvent3 = outboxEventRepository.save(outboxEvent3)

        doNothing().whenever(eventSender)
            .send(argThat<OutboxEvent>{ id == savedEvent1.id })
        doThrow(RuntimeException("Zaraza, zbiera się na burzę")).whenever(eventSender)
            .send(argThat<OutboxEvent>{ id == savedEvent2.id })

        assertThrows<RuntimeException>{outboxEventProcessor.processEventsForAggregateGroup(1L, OutboxAggregateType.MEDICAL_ORDER)}

        val inOrder = inOrder(eventSender)
        inOrder.verify(eventSender).send(argThat<OutboxEvent>{ id == savedEvent1.id })
        inOrder.verify(eventSender).send(argThat<OutboxEvent>{ id == savedEvent2.id })

        val allEvents = outboxEventRepository.findEventsForProcessing(1, OutboxAggregateType.MEDICAL_ORDER)
        assertThat(allEvents).hasSize(3)
        assertThat(allEvents[0].processedAt).isNull()
        assertThat(allEvents[1].processedAt).isNull()
        assertThat(allEvents[2].processedAt).isNull()

        verify(eventSender, never()).send(argThat<OutboxEvent>{ id == savedEvent3.id })
    }
}