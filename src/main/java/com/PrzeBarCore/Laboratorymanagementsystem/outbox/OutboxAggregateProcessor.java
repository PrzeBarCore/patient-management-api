package com.PrzeBarCore.Laboratorymanagementsystem.outbox;

import com.PrzeBarCore.Laboratorymanagementsystem.entity.OutboxEvent;
import com.PrzeBarCore.Laboratorymanagementsystem.global.AggregateType;
import com.PrzeBarCore.Laboratorymanagementsystem.repository.OutboxEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OutboxAggregateProcessor {
    private final EventSender eventSender;
    private final OutboxEventRepository outboxEventRepository;

    public OutboxAggregateProcessor(EventSender eventSender, OutboxEventRepository outboxEventRepository){
        this.eventSender = eventSender;
        this.outboxEventRepository = outboxEventRepository;
    }

    @Transactional
    public void processEventsForAggregate(Long aggregateId, AggregateType aggregateType){
        List<OutboxEvent> eventsForProcessing = outboxEventRepository.findEventsForProcessing(aggregateId, aggregateType);
        eventsForProcessing.forEach(this::processEvent);
    }

    private void processEvent(OutboxEvent event){
        eventSender.send(event);
        event.setProcessedAt(LocalDateTime.now());
    }
}
