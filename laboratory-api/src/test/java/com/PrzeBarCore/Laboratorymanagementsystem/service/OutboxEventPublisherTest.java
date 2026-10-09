package com.przebarcore.laboratoryapi.service;

import com.przebarcore.laboratoryapi.entity.OutboxEvent;
import com.przebarcore.laboratoryapi.global.AggregateType;
import com.przebarcore.laboratoryapi.outbox.OutboxAggregateProcessor;
import com.przebarcore.laboratoryapi.outbox.OutboxEventPublisher;
import com.przebarcore.laboratoryapi.repository.OutboxEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OutboxEventPublisherTest {
    @Mock
    OutboxEventRepository repository;
    @Mock
    OutboxAggregateProcessor aggregateProcessor;

    @InjectMocks
    OutboxEventPublisher outboxEventPublisher;

    @Test
    void shouldExtractAggregateIdsAndAggregateTypeAndPassItForProcessing() {
        //Arrange
        var outboxEvent1 = new OutboxEvent();
        outboxEvent1.setAggregateType(AggregateType.MEDICAL_ORDER);
        outboxEvent1.setAggregateId(1L);
        var outboxEvent2 = new OutboxEvent();
        outboxEvent2.setAggregateType(AggregateType.MEDICAL_ORDER);
        outboxEvent2.setAggregateId(1L);
        var outboxEvent3 = new OutboxEvent();
        outboxEvent3.setAggregateType(AggregateType.MEDICAL_ORDER);
        outboxEvent3.setAggregateId(2L);
        var outboxEvent4 = new OutboxEvent();
        outboxEvent4.setAggregateType(AggregateType.PATIENT);
        outboxEvent4.setAggregateId(2L);

        when(repository.findAllByProcessedAtIsNull()).thenReturn(List.of(outboxEvent1, outboxEvent2, outboxEvent3, outboxEvent4));

        //Act
        outboxEventPublisher.processEvents();

        //Assert
        verify(aggregateProcessor).processEventsForAggregate(1L, AggregateType.MEDICAL_ORDER);
        verify(aggregateProcessor).processEventsForAggregate(2L, AggregateType.MEDICAL_ORDER);
        verify(aggregateProcessor).processEventsForAggregate(2L, AggregateType.PATIENT);
        verifyNoMoreInteractions(aggregateProcessor);
    }

    @Test
    void shouldProcessAggregateEvenIfProcessingOfPreviousFails(){
        //Arrange
        var outboxEvent1 = new OutboxEvent();
        outboxEvent1.setAggregateType(AggregateType.MEDICAL_ORDER);
        outboxEvent1.setAggregateId(1L);
        var outboxEvent2 = new OutboxEvent();
        outboxEvent2.setAggregateType(AggregateType.MEDICAL_ORDER);
        outboxEvent2.setAggregateId(1L);
        var outboxEvent3 = new OutboxEvent();
        outboxEvent3.setAggregateType(AggregateType.MEDICAL_ORDER);
        outboxEvent3.setAggregateId(2L);

        when(repository.findAllByProcessedAtIsNull()).thenReturn(List.of(outboxEvent1, outboxEvent2, outboxEvent3));
        doThrow(new RuntimeException("Simulated failure")).when(aggregateProcessor).processEventsForAggregate(1L, AggregateType.MEDICAL_ORDER);
        //Act
        outboxEventPublisher.processEvents();

        //Assert
        verify(aggregateProcessor).processEventsForAggregate(1L, AggregateType.MEDICAL_ORDER);
        verify(aggregateProcessor).processEventsForAggregate(2L, AggregateType.MEDICAL_ORDER);
    }

}
