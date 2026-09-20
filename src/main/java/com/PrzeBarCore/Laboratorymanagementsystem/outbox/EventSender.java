package com.PrzeBarCore.Laboratorymanagementsystem.outbox;

import com.PrzeBarCore.Laboratorymanagementsystem.entity.OutboxEvent;

public interface EventSender {
    void send(OutboxEvent event);
}
