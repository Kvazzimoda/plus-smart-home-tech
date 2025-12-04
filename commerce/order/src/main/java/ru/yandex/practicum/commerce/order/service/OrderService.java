package ru.yandex.practicum.commerce.order.service;

import ru.yandex.practicum.commerce.interaction.dto.order.*;

import java.util.List;
import java.util.UUID;

public interface OrderService {

    List<OrderDto> getClientOrders(String username);

    OrderDto createNewOrder(String username, CreateOrderRequest request);

    OrderDto productReturn(ProductReturnRequest request);

    OrderDto payment(UUID orderId);

    OrderDto paymentFailed(UUID orderId);

    OrderDto delivery(UUID orderId);

    OrderDto deliveryFailed(UUID orderId);

    OrderDto complete(UUID orderId);

    OrderDto calculateTotalCost(UUID orderId);

    OrderDto calculateDeliveryCost(UUID orderId);

    OrderDto assembly(UUID orderId);

    OrderDto assemblyFailed(UUID orderId);

    OrderDto getOrderById(UUID orderId);

    OrderDto updateOrderState(UUID orderId, String username, OrderState newState);
}

