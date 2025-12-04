package ru.yandex.practicum.commerce.interaction.client.shopping.store;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.yandex.practicum.commerce.interaction.dto.shopping.store.ProductDto;

import java.util.UUID;

@FeignClient(
        name = "shopping-store",
        path = "/api/v1/shopping-store",
        fallbackFactory = ShoppingStoreClientFallbackFactory.class
)
public interface ShoppingStoreClient extends ShoppingStoreOperations {
}
