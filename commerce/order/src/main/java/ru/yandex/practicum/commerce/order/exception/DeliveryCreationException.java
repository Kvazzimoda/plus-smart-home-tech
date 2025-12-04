package ru.yandex.practicum.commerce.order.exception;

public class DeliveryCreationException extends RuntimeException {
    public DeliveryCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
