package ru.yandex.practicum.commerce.interaction.client.payment;

import ru.yandex.practicum.commerce.interaction.dto.order.OrderDto;
import ru.yandex.practicum.commerce.interaction.dto.payment.PaymentDto;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentOperations {

    // Рассчитать стоимость товаров в заказе
    BigDecimal productCost(OrderDto orderDto);

    // Расчёт полной стоимости заказа
    BigDecimal getTotalCost(OrderDto orderDto);

    // Формирование оплаты для заказа (переход в платежный шлюз)
    PaymentDto payment(OrderDto orderDto);

    // Метод для эмуляции успешной оплаты в платежного шлюза
    void paymentSuccess(UUID paymentId);

    // Метод для эмуляции отказа в оплате платежного шлюза
    void paymentFailed(UUID paymentId);
}
