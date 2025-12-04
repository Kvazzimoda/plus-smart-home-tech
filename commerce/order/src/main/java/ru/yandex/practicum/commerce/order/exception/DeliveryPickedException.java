package ru.yandex.practicum.commerce.order.exception;

public class DeliveryPickedException extends RuntimeException {
    public DeliveryPickedException(String message, Throwable cause) {
        super(message, cause);
    }
}
