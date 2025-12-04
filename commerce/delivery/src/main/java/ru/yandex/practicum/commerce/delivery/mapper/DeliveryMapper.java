package ru.yandex.practicum.commerce.delivery.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.delivery.model.Address;
import ru.yandex.practicum.commerce.delivery.model.Delivery;
import ru.yandex.practicum.commerce.interaction.dto.delivery.DeliveryDto;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.AddressDto;

@Component
public class DeliveryMapper {

    public DeliveryDto toDto(Delivery delivery) {
        return DeliveryDto.builder()
                .deliveryId(delivery.getDeliveryId())
                .orderId(delivery.getOrderId())
                .fromAddress(toAddressDto(delivery.getFromAddress()))
                .toAddress(toAddressDto(delivery.getToAddress()))
                .deliveryState(delivery.getDeliveryState())
                .totalWeight(delivery.getTotalWeight())
                .totalVolume(delivery.getTotalVolume())
                .fragile(delivery.getFragile())
                .deliveryCost(delivery.getDeliveryCost())
                .createdAt(delivery.getCreatedAt())
                .updatedAt(delivery.getUpdatedAt())
                .build();
    }

    public Delivery toEntity(DeliveryDto dto) {
        return Delivery.builder()
                .deliveryId(dto.getDeliveryId())
                .orderId(dto.getOrderId())
                .fromAddress(toAddressEntity(dto.getFromAddress()))
                .toAddress(toAddressEntity(dto.getToAddress()))
                .deliveryState(dto.getDeliveryState())
                .totalWeight(dto.getTotalWeight())
                .totalVolume(dto.getTotalVolume())
                .fragile(dto.getFragile())
                .deliveryCost(dto.getDeliveryCost())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }

    private AddressDto toAddressDto(Address address) {
        if (address == null) return null;
        return AddressDto.builder()
                .country(address.getCountry())
                .city(address.getCity())
                .street(address.getStreet())
                .house(address.getHouse())
                .flat(address.getFlat())
                .build();
    }

    private Address toAddressEntity(AddressDto dto) {
        if (dto == null) return null;
        return Address.builder()
                .country(dto.getCountry())
                .city(dto.getCity())
                .street(dto.getStreet())
                .house(dto.getHouse())
                .flat(dto.getFlat())
                .build();
    }
}
