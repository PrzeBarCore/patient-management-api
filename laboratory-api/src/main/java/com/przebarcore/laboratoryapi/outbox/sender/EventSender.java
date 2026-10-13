package com.przebarcore.laboratoryapi.outbox.sender;

import com.przebarcore.laboratoryapi.entity.OutboxEvent;

public interface EventSender {
    void send(OutboxEvent event);
}
