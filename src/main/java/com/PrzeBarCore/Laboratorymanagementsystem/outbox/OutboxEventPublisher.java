package com.PrzeBarCore.Laboratorymanagementsystem.outbox;

import com.PrzeBarCore.Laboratorymanagementsystem.entity.OutboxEvent;
import com.PrzeBarCore.Laboratorymanagementsystem.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OutboxEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(OutboxEventPublisher.class);
    private final OutboxEventRepository repository;

    OutboxEventPublisher(OutboxEventRepository repository) {
    this.repository = repository;
    }

    @Transactional
    public void processEvents(){
        List<OutboxEvent> eventsToProcess = repository.findAllByProcessedAtIsNull();
        eventsToProcess.forEach(this::processEvent);

    }

    private void processEvent(OutboxEvent event){
        log.info("Publish event: {}", event.getId());
        event.setProcessedAt(LocalDateTime.now());
    }
}
