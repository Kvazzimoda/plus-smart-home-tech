package ru.yandex.practicum.commerce.interaction.client.delivery;

import ru.yandex.practicum.commerce.interaction.dto.delivery.DeliveryDto;
import ru.yandex.practicum.commerce.interaction.dto.order.OrderDto;

import java.math.BigDecimal;
import java.util.UUID;

public interface DeliveryOperations {

    // Создать новую доставку в БД
    DeliveryDto delivery(DeliveryDto deliveryDto);

    // Расчёт полной стоимости доставки заказа
    BigDecimal deliveryCost(OrderDto orderDto);

    // Эмуляция получения товара в доставку
    void deliveryPicked(UUID orderId);

    // Эмуляция успешной доставки товара
    void deliverySuccessful(UUID orderId);

    // Эмуляция неудачного вручения товара
    void deliveryFailed(UUID orderId);
}