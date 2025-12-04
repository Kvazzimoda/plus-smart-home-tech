package ru.yandex.practicum.commerce.order.exception;

public class TotalCostCalculationException extends RuntimeException {
    public TotalCostCalculationException(String message, Throwable cause) {
        super(message, cause);
    }
}
