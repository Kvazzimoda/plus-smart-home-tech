package ru.yandex.practicum.commerce.interaction.dto.delivery;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.AddressDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryDto {

    @NotNull(message = "Delivery ID is required")
    private UUID deliveryId;

    @NotNull(message = "Order ID is required")
    private UUID orderId;

    @NotNull(message = "From address is required")
    @Valid
    private AddressDto fromAddress;

    @NotNull(message = "To address is required")
    @Valid
    private AddressDto toAddress;

    @NotNull(message = "Delivery state is required")
    private DeliveryState deliveryState;

    @DecimalMin(value = "0.0", message = "Total weight must be positive")
    private Double totalWeight;

    @DecimalMin(value = "0.0", message = "Total volume must be positive")
    private Double totalVolume;

    private Boolean fragile;

    @DecimalMin(value = "0.0", message = "Delivery cost must be positive")
    private BigDecimal deliveryCost;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
