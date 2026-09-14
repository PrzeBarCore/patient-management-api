package com.PrzeBarCore.Laboratorymanagementsystem.exception;

import com.PrzeBarCore.Laboratorymanagementsystem.global.AggregateType;

public class EventSerializationException extends RuntimeException {
    public EventSerializationException(AggregateType aggregateType, Long aggregateId, Throwable cause) {
        super("Serialization exception for: " + aggregateType.name() + " of id " + aggregateId, cause);
    }

}
