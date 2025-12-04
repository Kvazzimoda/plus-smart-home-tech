package ru.yandex.practicum.commerce.shopping.cart.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.commerce.interaction.dto.shopping.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.shopping.cart.model.ShoppingCartEntity;
import ru.yandex.practicum.commerce.shopping.cart.model.ShoppingCartItemEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ShoppingCartMapper {

    /**
     * Конвертирует Entity корзины и список items в DTO
     */
    public static ShoppingCartDto toDto(ShoppingCartEntity shoppingCart, List<ShoppingCartItemEntity> items) {
        if (shoppingCart == null) {
            return null;
        }

        Map<UUID, Long> productsMap = items.stream()
                .collect(Collectors.toMap(
                        ShoppingCartItemEntity::getProductId,
                        ShoppingCartItemEntity::getQuantity
                ));

        return ShoppingCartDto.builder()
                .shoppingCartId(shoppingCart.getShoppingCartId())
                .products(productsMap)
                .build();
    }

    /**
     * Создает новую корзину для пользователя
     */
    public static ShoppingCartEntity toNewEntity(String username) {
        return ShoppingCartEntity.builder()
                .username(username)
                .build();
    }

    /**
     * Создает новый элемент корзины
     */
    public static ShoppingCartItemEntity toNewItemEntity(ShoppingCartEntity shoppingCart, UUID productId, Long quantity) {
        return ShoppingCartItemEntity.builder()
                .shoppingCart(shoppingCart)
                .productId(productId)
                .quantity(quantity)
                .build();
    }
}
