package ru.yandex.practicum.commerce.order.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.interaction.dto.order.OrderDto;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.AddressDto;
import ru.yandex.practicum.commerce.order.model.Order;

import java.util.HashMap;

@Component
public class OrderMapper {

    public static OrderDto toDto(Order order) {
        return OrderDto.builder()
                .orderId(order.getOrderId())
                .shoppingCartId(order.getShoppingCartId())
                .userName(order.getUsername())
                .products(new HashMap<>(order.getProducts()))
                .paymentId(order.getPaymentId())
                .deliveryId(order.getDeliveryId())
                .state(order.getOrderState())
                .deliveryWeight(order.getDeliveryWeight())
                .deliveryVolume(order.getDeliveryVolume())
                .fragile(order.getFragile())
                .totalPrice(order.getTotalPrice())
                .deliveryPrice(order.getDeliveryPrice())
                .productPrice(order.getProductPrice())
                .deliveryAddress(AddressDto.builder()
                        .country(order.getCountry())
                        .city(order.getCity())
                        .street(order.getStreet())
                        .house(order.getHouse())
                        .flat(order.getFlat())
                        .build())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    public static Order toEntity(OrderDto dto) {
        return Order.builder()
                .orderId(dto.getOrderId())
                .username(dto.getUserName())
                .shoppingCartId(dto.getShoppingCartId())
                .products(new HashMap<>(dto.getProducts()))
                .paymentId(dto.getPaymentId())
                .deliveryId(dto.getDeliveryId())
                .orderState(dto.getState())
                .deliveryWeight(dto.getDeliveryWeight())
                .deliveryVolume(dto.getDeliveryVolume())
                .fragile(dto.getFragile())
                .totalPrice(dto.getTotalPrice())
                .deliveryPrice(dto.getDeliveryPrice())
                .productPrice(dto.getProductPrice())
                .country(dto.getDeliveryAddress() != null ? dto.getDeliveryAddress().getCountry() : null)
                .city(dto.getDeliveryAddress() != null ? dto.getDeliveryAddress().getCity() : null)
                .street(dto.getDeliveryAddress() != null ? dto.getDeliveryAddress().getStreet() : null)
                .house(dto.getDeliveryAddress() != null ? dto.getDeliveryAddress().getHouse() : null)
                .flat(dto.getDeliveryAddress() != null ? dto.getDeliveryAddress().getFlat() : null)
                .build();
    }
}

