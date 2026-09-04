package com.PrzeBarCore.Laboratorymanagementsystem.exception;

import com.PrzeBarCore.Laboratorymanagementsystem.global.OrderStatus;

public class InvalidOrderStatusTransitionException extends RuntimeException {
    public InvalidOrderStatusTransitionException(OrderStatus currentStatus, OrderStatus status) {
        super("Cannot change order status from " + currentStatus + " to " + status );
    }
}
