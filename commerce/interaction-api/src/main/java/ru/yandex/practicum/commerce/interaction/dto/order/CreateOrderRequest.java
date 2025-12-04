package ru.yandex.practicum.commerce.interaction.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.commerce.interaction.dto.shopping.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.AddressDto;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotNull(message = "Shopping cart is required")
    @Valid
    private ShoppingCartDto shoppingCart;

    @NotNull(message = "Delivery address is required")
    @Valid
    private AddressDto deliveryAddress;
}


