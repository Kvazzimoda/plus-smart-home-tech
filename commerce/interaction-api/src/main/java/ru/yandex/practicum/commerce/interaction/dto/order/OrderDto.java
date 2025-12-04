package ru.yandex.practicum.commerce.interaction.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.AddressDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {

    @NotNull(message = "Order ID is required")
    private UUID orderId;

    private UUID shoppingCartId;

    @NotBlank(message = "Username is required")
    private String userName;

    @NotNull(message = "Products cannot be null")
    @NotEmpty(message = "Products cannot be empty")
    private Map<UUID, Long> products;

    private UUID paymentId;
    private UUID deliveryId;

    @NotNull(message = "State is required")
    private OrderState state;

    @Valid
    private AddressDto deliveryAddress;

    @DecimalMin(value = "0.0", message = "Delivery weight must be positive")
    private Double deliveryWeight;

    @DecimalMin(value = "0.0", message = "Delivery volume must be positive")
    private Double deliveryVolume;

    private Boolean fragile;

    @DecimalMin(value = "0.0", message = "Total price must be positive")
    private BigDecimal totalPrice;

    @DecimalMin(value = "0.0", message = "Delivery price must be positive")
    private BigDecimal deliveryPrice;

    @DecimalMin(value = "0.0", message = "Product price must be positive")
    private BigDecimal productPrice;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}