package ru.yandex.practicum.commerce.order.exception;

public class PaymentCreationException extends RuntimeException {
    public PaymentCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
