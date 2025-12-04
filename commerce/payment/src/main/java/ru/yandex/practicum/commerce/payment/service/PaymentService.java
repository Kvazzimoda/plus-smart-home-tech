package ru.yandex.practicum.commerce.payment.service;

import ru.yandex.practicum.commerce.interaction.dto.order.OrderDto;
import ru.yandex.practicum.commerce.interaction.dto.payment.PaymentDto;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentService {

    BigDecimal calculateProductCost(OrderDto order);

    BigDecimal calculateTotalCost(OrderDto order);

    PaymentDto payment(OrderDto order);

    PaymentDto getPayment(UUID paymentId);

    PaymentDto markPaymentSuccess(UUID paymentId);

    PaymentDto markPaymentFailed(UUID paymentId);

    PaymentDto getPaymentByOrderId(UUID orderId);
}
