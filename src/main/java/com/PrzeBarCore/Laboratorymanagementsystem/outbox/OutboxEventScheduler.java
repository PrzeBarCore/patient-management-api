package com.PrzeBarCore.Laboratorymanagementsystem.outbox;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutboxEventScheduler {
    private final OutboxEventPublisher outboxEventPublisher;

    public OutboxEventScheduler(OutboxEventPublisher outboxEventPublisher) {
        this.outboxEventPublisher = outboxEventPublisher;
    }

    @Scheduled(fixedDelay = 5000L)
    void publishOutboxEvents(){
        outboxEventPublisher.processEvents();
    }
}
