package ru.yandex.practicum.commerce.interaction.client.warehouse;

import ru.yandex.practicum.commerce.interaction.dto.delivery.ShippedToDeliveryRequest;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.*;
import ru.yandex.practicum.commerce.interaction.dto.shopping.cart.ShoppingCartDto;

import java.util.Map;
import java.util.UUID;

public interface WarehouseOperations {

    void newProductInWarehouse(NewProductInWarehouseRequest request);

    BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto shoppingCartDto);

    BookedProductsDto assemblyProductsForOrder(AssemblyProductsForOrderRequest request);

    void addProductToWarehouse(AddProductToWarehouseRequest request);

    AddressDto getWarehouseAddress();

    void acceptReturn(Map<UUID, Long> returnedProducts);

    void shippedToDelivery(ShippedToDeliveryRequest request);
}