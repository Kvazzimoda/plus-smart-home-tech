package ru.yandex.practicum.commerce.interaction.client.warehouse;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.interaction.dto.delivery.ShippedToDeliveryRequest;
import ru.yandex.practicum.commerce.interaction.dto.shopping.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.*;
import ru.yandex.practicum.commerce.interaction.exception.WarehouseServiceUnavailableException;

import java.util.Map;
import java.util.UUID;

@Component
public class WarehouseClientFallback implements WarehouseClient {

    @Override
    public void newProductInWarehouse(NewProductInWarehouseRequest request) {
        throw new WarehouseServiceUnavailableException("Warehouse service is unavailable");
    }

    @Override
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto shoppingCartDto) {
        throw new WarehouseServiceUnavailableException("Warehouse service is unavailable");
    }

    @Override
    public BookedProductsDto assemblyProductsForOrder(AssemblyProductsForOrderRequest request) {
        throw new WarehouseServiceUnavailableException("Warehouse service is unavailable");
    }

    @Override
    public void addProductToWarehouse(AddProductToWarehouseRequest request) {
        throw new WarehouseServiceUnavailableException("Warehouse service is unavailable");
    }

    @Override
    public AddressDto getWarehouseAddress() {
        throw new WarehouseServiceUnavailableException("Warehouse service is unavailable");
    }

    @Override
    public void acceptReturn(Map<UUID, Long> returnedProducts) {
        throw new WarehouseServiceUnavailableException("Warehouse service is unavailable");
    }

    @Override
    public void shippedToDelivery(ShippedToDeliveryRequest request) {
        throw new WarehouseServiceUnavailableException("Warehouse service is unavailable");
    }
}