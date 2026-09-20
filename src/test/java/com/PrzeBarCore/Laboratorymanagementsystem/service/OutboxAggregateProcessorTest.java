package com.PrzeBarCore.Laboratorymanagementsystem.service;

import com.PrzeBarCore.Laboratorymanagementsystem.entity.OutboxEvent;
import com.PrzeBarCore.Laboratorymanagementsystem.global.AggregateType;
import com.PrzeBarCore.Laboratorymanagementsystem.outbox.EventSender;
import com.PrzeBarCore.Laboratorymanagementsystem.outbox.OutboxAggregateProcessor;
import com.PrzeBarCore.Laboratorymanagementsystem.repository.OutboxEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OutboxAggregateProcessorTest {
    @Mock
    private OutboxEventRepository repository;
    @Mock
    private EventSender eventSender;

    @InjectMocks
    private OutboxAggregateProcessor processor;

    @Test
    void shouldNotSendSecondEventIfFirstFails(){
        //Arrange
        var event1 = new OutboxEvent();
        event1.setAggregateId(1L);
        event1.setAggregateType(AggregateType.MEDICAL_ORDER);
        var event2 = new OutboxEvent();
        event2.setAggregateId(1L);
        event2.setAggregateType(AggregateType.MEDICAL_ORDER);

        when(repository.findEventsForProcessing(1L, AggregateType.MEDICAL_ORDER)).thenReturn(List.of(event1, event2));
        doThrow(new RuntimeException("Simulated Failure")).when(eventSender).send(event1);

        //Act
        assertThrows(RuntimeException.class, () -> processor.processEventsForAggregate(1L, AggregateType.MEDICAL_ORDER));

        //Assert
        assertThat(event2.getProcessedAt()).isNull();
        verify(eventSender).send(event1);
        verify(eventSender, never()).send(event2);
    }

    @Test
    void shouldProcessEvent() {
        //Arrange
        var outboxEvent = new OutboxEvent();
        outboxEvent.setAggregateId(1L);
        outboxEvent.setAggregateType(AggregateType.MEDICAL_ORDER);

        when(repository.findEventsForProcessing(1L, AggregateType.MEDICAL_ORDER)).thenReturn(List.of(outboxEvent));

        //Act
        processor.processEventsForAggregate(1L, AggregateType.MEDICAL_ORDER);

        //Assert
        assertThat(outboxEvent.getProcessedAt()).isNotNull();
        verify(eventSender).send(outboxEvent);
    }
}
