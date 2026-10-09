package com.przebarcore.laboratoryapi.outbox;

import com.przebarcore.laboratoryapi.entity.OutboxEvent;
import com.przebarcore.laboratoryapi.global.AggregateType;
import com.przebarcore.laboratoryapi.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OutboxEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(OutboxEventPublisher.class);
    private final OutboxEventRepository repository;
    private final OutboxAggregateProcessor outboxAggregateProcessor;

    OutboxEventPublisher(OutboxEventRepository repository, OutboxAggregateProcessor outboxAggregateProcessor) {
        this.repository = repository;
        this.outboxAggregateProcessor = outboxAggregateProcessor;
    }

    public void processEvents() {
        List<OutboxEvent> allEventsToProcess = repository.findAllByProcessedAtIsNull();
        List<Long> aggregateIds = allEventsToProcess.stream().map(OutboxEvent::getAggregateId).distinct().toList();
        for (Long aggregateId : aggregateIds) {
            var aggregateTypes = allEventsToProcess.stream().filter(event -> event.getAggregateId().equals(aggregateId)).map(OutboxEvent::getAggregateType).distinct().toList();
            for(AggregateType aggregateType : aggregateTypes){
                try{
                    outboxAggregateProcessor.processEventsForAggregate(aggregateId, aggregateType);
                }
                catch (RuntimeException exception){
                    log.error("Error when processing events for aggregate: " + aggregateId + " of type: " + aggregateType + " " + exception);
                }
            }

        }
    }
}
