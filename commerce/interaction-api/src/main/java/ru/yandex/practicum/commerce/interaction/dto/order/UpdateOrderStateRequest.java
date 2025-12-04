package ru.yandex.practicum.commerce.interaction.dto.order;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderStateRequest {

    @NotNull(message = "State is required")
    private OrderState state;

    private String comment;
    private String updatedBy;

    public UpdateOrderStateRequest(OrderState state) {
        this.state = state;
    }
}
