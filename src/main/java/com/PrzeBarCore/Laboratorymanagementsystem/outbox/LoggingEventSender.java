package com.PrzeBarCore.Laboratorymanagementsystem.outbox;

import com.PrzeBarCore.Laboratorymanagementsystem.entity.OutboxEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class LoggingEventSender implements EventSender{
    private static final Logger log = LoggerFactory.getLogger(LoggingEventSender.class);
    @Override
    public void send(OutboxEvent event) {
        log.info("Publish event: {}", event.getId());
    }
}
