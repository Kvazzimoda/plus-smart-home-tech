package ru.yandex.practicum.commerce.payment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.interaction.client.payment.PaymentOperations;
import ru.yandex.practicum.commerce.interaction.dto.order.OrderDto;
import ru.yandex.practicum.commerce.interaction.dto.payment.PaymentDto;
import ru.yandex.practicum.commerce.payment.service.PaymentService;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController implements PaymentOperations {

    private final PaymentService paymentService;

    @Override
    @PostMapping("/cost/products")
    public BigDecimal productCost(@RequestBody OrderDto orderDto) {
        return paymentService.calculateProductCost(orderDto);
    }

    @Override
    @PostMapping("/cost/total")
    public BigDecimal getTotalCost(@RequestBody OrderDto orderDto) {
        return paymentService.calculateTotalCost(orderDto);
    }

    @Override
    @PostMapping
    public PaymentDto payment(@RequestBody OrderDto orderDto) {
        return paymentService.payment(orderDto);
    }

    @Override
    @PostMapping("/{paymentId}/success")
    public void paymentSuccess(@PathVariable UUID paymentId) {
        paymentService.markPaymentSuccess(paymentId);
    }

    @Override
    @PostMapping("/{paymentId}/failed")
    public void paymentFailed(@PathVariable UUID paymentId) {
        paymentService.markPaymentFailed(paymentId);
    }

    // Добавьте дополнительные endpoints
    @GetMapping("/{paymentId}")
    public PaymentDto getPayment(@PathVariable UUID paymentId) {
        return paymentService.getPayment(paymentId);
    }

    @GetMapping("/order/{orderId}")
    public PaymentDto getPaymentByOrderId(@PathVariable UUID orderId) {
        return paymentService.getPaymentByOrderId(orderId);
    }
}
