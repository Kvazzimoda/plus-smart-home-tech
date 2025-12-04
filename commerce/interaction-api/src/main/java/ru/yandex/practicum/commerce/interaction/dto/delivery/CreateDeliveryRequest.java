package ru.yandex.practicum.commerce.interaction.dto.delivery;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.AddressDto;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDeliveryRequest {
    @NotNull
    private UUID orderId;

    @NotNull
    private AddressDto fromAddress;

    @NotNull
    private AddressDto toAddress;

    private Double totalWeight;
    private Double totalVolume;
    private Boolean fragile;
}
