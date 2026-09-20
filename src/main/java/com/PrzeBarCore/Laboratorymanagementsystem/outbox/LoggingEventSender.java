package com.PrzeBarCore.Laboratorymanagementsystem.outbox;

import com.PrzeBarCore.Laboratorymanagementsystem.entity.OutboxEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingEventSender implements EventSender{
    private static final Logger log = LoggerFactory.getLogger(LoggingEventSender.class);
    @Override
    public void send(OutboxEvent event) {
        log.info("Publish event: {}", event.getId());
    }
}
