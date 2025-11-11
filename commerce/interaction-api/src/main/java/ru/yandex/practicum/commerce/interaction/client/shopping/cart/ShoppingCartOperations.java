package ru.yandex.practicum.commerce.interaction.client.shopping.cart;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.interaction.client.shopping.cart.exception.*;
import ru.yandex.practicum.commerce.interaction.dto.shopping.cart.ShoppingCartDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ShoppingCartOperations {

    ShoppingCartDto getShoppingCart(String username);

    ShoppingCartDto addProductToShoppingCart(String username, Map<UUID, Integer> products);

    void deactivateCurrentShoppingCart(String username);

    ShoppingCartDto removeFromShoppingCart(String username, List<UUID> productIds);

    ShoppingCartDto changeProductQuantity(String username, ChangeProductQuantityRequest request);
}
