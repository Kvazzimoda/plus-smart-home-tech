package ru.yandex.practicum.commerce.interaction.exception;

public class WarehouseServiceUnavailableException extends RuntimeException {
    public WarehouseServiceUnavailableException(String message) {
        super(message);
    }
}
