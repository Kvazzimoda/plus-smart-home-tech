package ru.yandex.practicum.commerce.interaction.client.warehouse;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.commerce.interaction.dto.shopping.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.BookedProductsDto;

@FeignClient(
        name = "warehouse",
        path = "/api/v1/warehouse",
        fallbackFactory = WarehouseClientFallbackFactory.class
)
public interface WarehouseClient extends WarehouseOperations {
    @PostMapping("/api/v1/warehouse/check")
    BookedProductsDto checkAvailability(@RequestBody ShoppingCartDto shoppingCartDto);
}
