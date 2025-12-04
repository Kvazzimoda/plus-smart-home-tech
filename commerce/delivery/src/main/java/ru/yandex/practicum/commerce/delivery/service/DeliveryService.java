package ru.yandex.practicum.commerce.delivery.service;

import ru.yandex.practicum.commerce.interaction.dto.delivery.DeliveryDto;
import ru.yandex.practicum.commerce.interaction.dto.order.OrderDto;

import java.math.BigDecimal;
import java.util.UUID;

public interface DeliveryService {

    // Создать доставку на основе DeliveryDto
    DeliveryDto createDelivery(DeliveryDto deliveryDto);

    // Рассчитать стоимость доставки для заказа
    BigDecimal calculateDeliveryCost(OrderDto orderDto);

    // Обработать получение товара в доставку (picked)
    void processDeliveryPicked(UUID orderId);

    // Обработать успешную доставку
    void processDeliverySuccess(UUID orderId);

    // Обработать неудачную доставку
    void processDeliveryFailed(UUID orderId);

    // Дополнительные методы (опционально)
    DeliveryDto getDeliveryById(UUID deliveryId);

    DeliveryDto getDeliveryByOrderId(UUID orderId);
}
