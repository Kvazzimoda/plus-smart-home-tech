package ru.yandex.practicum.commerce.delivery.exception;

import java.util.UUID;

public class DeliveryNotFoundException extends RuntimeException {
    public DeliveryNotFoundException(UUID deliveryId) {
        super("Delivery not found with ID: " + deliveryId);
    }

    public DeliveryNotFoundException(String message) {
        super(message);
    }
}
