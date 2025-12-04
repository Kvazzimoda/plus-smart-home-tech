package ru.yandex.practicum.commerce.delivery.exception;

import java.util.UUID;

public class DeliveryAlreadyExistsException extends RuntimeException {
    public DeliveryAlreadyExistsException(UUID orderId) {
        super("Delivery already exists for order: " + orderId);
    }
}
