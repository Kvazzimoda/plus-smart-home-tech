package ru.yandex.practicum.commerce.order.exception;

import java.util.UUID;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(UUID orderId) {
        super("Order not found: " + orderId);
    }

    public OrderNotFoundException(UUID orderId, String message) {
        super("Order " + orderId + ": " + message);
    }
}
