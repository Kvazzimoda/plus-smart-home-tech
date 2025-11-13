package ru.yandex.practicum.commerce.interaction.client.shopping.cart;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.interaction.client.shopping.cart.exception.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.interaction.dto.shopping.cart.ShoppingCartDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class ShoppingCartClientFallbackFactory implements FallbackFactory<ShoppingCartClient> {

    @Override
    public ShoppingCartClient create(Throwable cause) {
        return new ShoppingCartClient() {

            @Override
            public ShoppingCartDto getShoppingCart(String username) {
                throw new RuntimeException("Shopping-cart service unavailable", cause);
            }

            @Override
            public ShoppingCartDto addProductToShoppingCart(String username, Map<UUID, Integer> products) {
                throw new RuntimeException("Shopping-cart service unavailable", cause);
            }

            @Override
            public void deactivateCurrentShoppingCart(String username) {
                throw new RuntimeException("Shopping-cart service unavailable", cause);
            }

            @Override
            public ShoppingCartDto removeFromShoppingCart(String username, List<UUID> products) {
                throw new RuntimeException("Shopping-cart service unavailable", cause);
            }

            @Override
            public ShoppingCartDto changeProductQuantity(String username, ChangeProductQuantityRequest request) {
                throw new RuntimeException("Shopping-cart service unavailable", cause);
            }
        };
    }
}
