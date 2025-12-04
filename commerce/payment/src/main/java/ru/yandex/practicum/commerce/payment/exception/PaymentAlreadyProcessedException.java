package ru.yandex.practicum.commerce.payment.exception;

import java.util.UUID;

public class PaymentAlreadyProcessedException extends RuntimeException {
    public PaymentAlreadyProcessedException(UUID id) {
        super("Payment is already processed: " + id);
    }
}
