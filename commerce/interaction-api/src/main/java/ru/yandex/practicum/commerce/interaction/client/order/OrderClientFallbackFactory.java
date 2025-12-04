package ru.yandex.practicum.commerce.interaction.client.order;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.interaction.dto.order.*;
import ru.yandex.practicum.commerce.interaction.exception.OrderServiceUnavailableException;

import java.util.List;
import java.util.UUID;

@Component
public class OrderClientFallbackFactory implements FallbackFactory<OrderClient> {

    @Override
    public OrderClient create(Throwable cause) {

        return new OrderClient() {

            @Override
            public List<OrderDto> getClientOrders(String username) {
                throw new OrderServiceUnavailableException("Order service is unavailable");
            }

            @Override
            public OrderDto createNewOrder(String username, CreateOrderRequest request) {
                throw new OrderServiceUnavailableException("Order service is unavailable");
            }

            @Override
            public OrderDto productReturn(ProductReturnRequest request) {
                throw new OrderServiceUnavailableException("Order service is unavailable");
            }

            @Override
            public OrderDto payment(UUID orderId) {
                throw new OrderServiceUnavailableException("Order service is unavailable");
            }

            @Override
            public OrderDto paymentFailed(UUID orderId) {
                throw new OrderServiceUnavailableException("Order service is unavailable");
            }

            @Override
            public OrderDto delivery(UUID orderId) {
                throw new OrderServiceUnavailableException("Order service is unavailable");
            }

            @Override
            public OrderDto deliveryFailed(UUID orderId) {
                throw new OrderServiceUnavailableException("Order service is unavailable");
            }

            @Override
            public OrderDto complete(UUID orderId) {
                throw new OrderServiceUnavailableException("Order service is unavailable");
            }

            @Override
            public OrderDto calculateTotalCost(UUID orderId) {
                throw new OrderServiceUnavailableException("Order service is unavailable");
            }

            @Override
            public OrderDto calculateDeliveryCost(UUID orderId) {
                throw new OrderServiceUnavailableException("Order service is unavailable");
            }

            @Override
            public OrderDto assembly(UUID orderId) {
                throw new OrderServiceUnavailableException("Order service is unavailable");
            }

            @Override
            public OrderDto assemblyFailed(UUID orderId) {
                throw new OrderServiceUnavailableException("Order service is unavailable");
            }

            @Override
            public OrderDto updateOrderState(UUID orderId, UpdateOrderStateRequest request) {
                throw new OrderServiceUnavailableException("Order service is unavailable");
            }
        };
    }
}