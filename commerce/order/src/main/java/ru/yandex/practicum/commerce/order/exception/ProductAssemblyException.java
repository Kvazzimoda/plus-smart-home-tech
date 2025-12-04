package ru.yandex.practicum.commerce.order.exception;

public class ProductAssemblyException extends RuntimeException {
    public ProductAssemblyException(String message, Throwable cause) {
        super(message, cause);
    }
}
