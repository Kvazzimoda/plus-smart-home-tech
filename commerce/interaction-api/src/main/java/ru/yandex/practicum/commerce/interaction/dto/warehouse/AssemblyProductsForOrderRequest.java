package ru.yandex.practicum.commerce.interaction.dto.warehouse;

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
public class AssemblyProductsForOrderRequest {

    @NotNull(message = "Order ID is required")
    private UUID orderId;

    @NotNull(message = "Products cannot be null")
    @NotEmpty(message = "Products cannot be empty")
    private Map<UUID, Long> products;

    private String assemblyNotes; // Опционально: примечания к сборке
    private String priority; // Опционально: приоритет сборки (NORMAL, HIGH, URGENT)
}
