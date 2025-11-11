package ru.yandex.practicum.commerce.interaction.client.warehouse;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.AddressDto;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.interaction.dto.shopping.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.NewProductInWarehouseRequest;

@Component
public class WarehouseClientFallbackFactory implements FallbackFactory<WarehouseClient> {

    @Override
    public WarehouseClient create(Throwable cause) {
        return new WarehouseClient() {

            @Override
            public void newProductInWarehouse(NewProductInWarehouseRequest request) {
                throw new RuntimeException("Warehouse service unavailable", cause);
            }

            @Override
            public BookedProductsDto checkAvailability(ShoppingCartDto shoppingCartDto) {
                throw new RuntimeException("Warehouse service unavailable", cause);
            }

            @Override
            public void addProductToWarehouse(AddProductToWarehouseRequest request) {
                throw new RuntimeException("Warehouse service unavailable", cause);
            }

            @Override
            public AddressDto getWarehouseAddress() {
                throw new RuntimeException("Warehouse service unavailable", cause);
            }
        };
    }
}
