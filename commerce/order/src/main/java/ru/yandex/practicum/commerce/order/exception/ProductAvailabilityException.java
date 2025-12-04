package ru.yandex.practicum.commerce.order.exception;

public class ProductAvailabilityException extends RuntimeException {
    public ProductAvailabilityException(String message, Throwable cause) {
        super(message, cause);
    }
}
