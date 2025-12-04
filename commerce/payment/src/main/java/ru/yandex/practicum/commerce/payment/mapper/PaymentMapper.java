package ru.yandex.practicum.commerce.payment.mapper;

import ru.yandex.practicum.commerce.interaction.dto.payment.PaymentDto;
import ru.yandex.practicum.commerce.payment.model.Payment;

public class PaymentMapper {

    public static PaymentDto toDto(Payment payment) {
        if (payment == null) return null;

        return new PaymentDto(
                payment.getPaymentId(),
                payment.getProductCost(),
                payment.getDeliveryCost(),
                payment.getTaxCost(),
                payment.getTotalCost(),
                payment.getPaymentStatus()
        );
    }
}