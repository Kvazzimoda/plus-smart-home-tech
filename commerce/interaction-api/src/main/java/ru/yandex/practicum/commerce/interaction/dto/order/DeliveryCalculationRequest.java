package ru.yandex.practicum.commerce.interaction.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryCalculationRequest {

    private Double weight;
    private Double volume;
    private Boolean fragile;
}

