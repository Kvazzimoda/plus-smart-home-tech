package ru.yandex.practicum.commerce.warehouse.service;

import ru.yandex.practicum.commerce.interaction.dto.delivery.ShippedToDeliveryRequest;
import ru.yandex.practicum.commerce.interaction.dto.shopping.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.*;

import java.util.Map;
import java.util.UUID;

public interface WarehouseService {

    BookedProductsDto assemblyProductsForOrder(AssemblyProductsForOrderRequest request);

    BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto shoppingCartDto);

    AddressDto getWarehouseAddress();

    void acceptReturn(Map<UUID, Long> returnedProducts);

    void newProductInWarehouse(NewProductInWarehouseRequest request);

    void addProductToWarehouse(AddProductToWarehouseRequest request);

    void shippedToDelivery(ShippedToDeliveryRequest request);
}
