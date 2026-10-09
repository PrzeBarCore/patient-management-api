package com.przebarcore.laboratoryapi.outbox;

import com.przebarcore.laboratoryapi.entity.OutboxEvent;

public interface EventSender {
    void send(OutboxEvent event);
}
