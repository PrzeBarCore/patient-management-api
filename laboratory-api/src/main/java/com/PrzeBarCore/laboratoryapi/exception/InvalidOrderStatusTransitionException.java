package com.przebarcore.laboratoryapi.exception;

import com.przebarcore.laboratoryapi.global.OrderStatus;

public class InvalidOrderStatusTransitionException extends RuntimeException {
    public InvalidOrderStatusTransitionException(OrderStatus currentStatus, OrderStatus status) {
        super("Cannot change order status from " + currentStatus + " to " + status );
    }
}
