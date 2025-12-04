package ru.yandex.practicum.commerce.interaction.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {
    private UUID paymentId;
    private BigDecimal productCost;
    private BigDecimal deliveryCost;
    private BigDecimal tax;         // налог
    private BigDecimal totalCost;   // итоговая сумма
    private PaymentStatus status;
}
