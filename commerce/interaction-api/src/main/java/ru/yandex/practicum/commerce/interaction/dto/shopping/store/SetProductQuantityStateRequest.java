package ru.yandex.practicum.commerce.interaction.dto.shopping.store;

import lombok.*;

import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SetProductQuantityStateRequest {

    private UUID productId;
    private QuantityState quantityState;
}
