package ru.yandex.practicum.commerce.interaction.dto.shopping.cart;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingCartDto {

    @NotNull(message = "Shopping cart ID is required")
    private UUID shoppingCartId;

    @NotNull(message = "Products cannot be null")
    @NotEmpty(message = "Products cannot be empty")
    private Map<UUID, Long> products;
}
