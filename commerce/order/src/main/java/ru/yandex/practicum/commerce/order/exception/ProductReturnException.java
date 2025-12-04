package ru.yandex.practicum.commerce.order.exception;

public class ProductReturnException extends RuntimeException {
    public ProductReturnException(String message, Throwable cause) {
        super(message, cause);
    }
}
