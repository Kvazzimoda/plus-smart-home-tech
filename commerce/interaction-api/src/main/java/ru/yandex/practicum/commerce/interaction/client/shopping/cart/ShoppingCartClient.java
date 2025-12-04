package ru.yandex.practicum.commerce.interaction.client.shopping.cart;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "shopping-cart", path = "/api/v1/shopping-cart", fallback = ShoppingCartClientFallback.class, fallbackFactory = ShoppingCartClientFallbackFactory.class)
public interface ShoppingCartClient extends ShoppingCartOperations {
}
