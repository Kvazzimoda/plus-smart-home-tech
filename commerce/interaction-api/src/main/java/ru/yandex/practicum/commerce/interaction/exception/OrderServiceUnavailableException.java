package ru.yandex.practicum.commerce.interaction.exception;

public class OrderServiceUnavailableException extends RuntimeException {
    public OrderServiceUnavailableException(String message) {
        super(message);
    }
}
