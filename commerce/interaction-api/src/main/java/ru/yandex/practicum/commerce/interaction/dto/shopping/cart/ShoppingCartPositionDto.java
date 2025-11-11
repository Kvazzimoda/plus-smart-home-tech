package ru.yandex.practicum.commerce.interaction.dto.shopping.cart;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingCartPositionDto {

    @NotNull
    private UUID productId;
    private String productName;
    private Long quantity;
    private Double price;

}
