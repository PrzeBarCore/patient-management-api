package com.PrzeBarCore.Laboratorymanagementsystem.outbox;

import com.PrzeBarCore.Laboratorymanagementsystem.entity.OutboxEvent;
import org.springframework.stereotype.Component;

@Component
public class SqsEventSender implements EventSender{
    @Override
    public void send(OutboxEvent event) {

    }
}
