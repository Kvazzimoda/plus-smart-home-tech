package ru.yandex.practicum.commerce.interaction.dto.order;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductReturnRequest {

    @NotNull(message = "Order ID is required")
    private UUID orderId;

    @NotNull(message = "Products cannot be null")
    private Map<UUID, Long> products;

    private String reason;
    private LocalDateTime returnDate;
}
