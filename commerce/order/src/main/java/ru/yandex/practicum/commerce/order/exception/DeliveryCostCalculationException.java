package ru.yandex.practicum.commerce.order.exception;

public class DeliveryCostCalculationException extends RuntimeException {
    public DeliveryCostCalculationException(String message, Throwable cause) {
        super(message, cause);
    }
}
