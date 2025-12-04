package ru.yandex.practicum.commerce.delivery.exception;

public class InvalidDeliveryStateException extends RuntimeException {
    public InvalidDeliveryStateException(String message) {
        super(message);
    }
}
