package ru.yandex.practicum.commerce.interaction.client.warehouse;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.interaction.dto.shopping.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.BookedProductsDto;

@Component
public class WarehouseClientFallback  implements WarehouseClient {
    @Override
    public BookedProductsDto checkAvailability(ShoppingCartDto shoppingCartDto) {
        throw new RuntimeException("Warehouse service is unavailable");
    }
}
