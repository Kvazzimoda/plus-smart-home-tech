package ru.yandex.practicum.commerce.payment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.interaction.client.order.OrderClient;
import ru.yandex.practicum.commerce.interaction.client.shopping.store.ShoppingStoreClient;
import ru.yandex.practicum.commerce.interaction.dto.order.OrderDto;
import ru.yandex.practicum.commerce.interaction.dto.payment.PaymentDto;
import ru.yandex.practicum.commerce.interaction.dto.payment.PaymentStatus;
import ru.yandex.practicum.commerce.payment.dal.PaymentRepository;
import ru.yandex.practicum.commerce.payment.exception.NotEnoughInfoInOrderToCalculateException;
import ru.yandex.practicum.commerce.payment.exception.PaymentNotFoundException;
import ru.yandex.practicum.commerce.payment.mapper.PaymentMapper;
import ru.yandex.practicum.commerce.payment.model.Payment;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = {"payments", "order-calculations", "product-prices"})
public class PaymentServiceImpl implements PaymentService {

    private static final BigDecimal TAX_RATE = BigDecimal.valueOf(0.1); // НДС 10%

    // Константы для имен кешей
    private static final String PAYMENTS_CACHE = "payments";
    private static final String ORDER_CALCULATIONS_CACHE = "order-calculations";
    private static final String PRODUCT_PRICES_CACHE = "product-prices";
    private static final String TRANSACTION_STATUS_CACHE = "transaction-status";

    private final PaymentRepository repository;
    private final ShoppingStoreClient shoppingStoreClient;
    private final OrderClient orderClient;

    @Override
    @Cacheable(value = ORDER_CALCULATIONS_CACHE,
            key = "'products_cost:' + #order.orderId + ':' + #order.products.hashCode()",
            unless = "#result == null")
    public BigDecimal calculateProductCost(OrderDto order) {
        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<UUID, Long> entry : order.getProducts().entrySet()) {
            UUID productId = entry.getKey();
            Long qty = entry.getValue();

            var product = shoppingStoreClient.getProduct(productId);
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(qty)));
        }

        return total;
    }

    @Override
    @Cacheable(value = ORDER_CALCULATIONS_CACHE,
            key = "'total_cost:' + #order.orderId + ':' + #order.deliveryPrice + ':' + #order.products.hashCode()",
            unless = "#result == null")
    public BigDecimal calculateTotalCost(OrderDto order) {
        BigDecimal productsCost = calculateProductCost(order);

        if (order.getDeliveryPrice() == null) {
            throw new NotEnoughInfoInOrderToCalculateException("Нет информации о доставке");
        }

        BigDecimal taxCost = productsCost.multiply(TAX_RATE);
        return productsCost.add(taxCost).add(order.getDeliveryPrice());
    }

    @Override
    @Caching(
            evict = {
                    @CacheEvict(value = ORDER_CALCULATIONS_CACHE,
                            key = "'*' + #order.orderId + '*'",  // Удаляем все расчеты для этого orderId
                            allEntries = false),
                    @CacheEvict(value = TRANSACTION_STATUS_CACHE, allEntries = true)
            },
            put = {
                    @CachePut(value = PAYMENTS_CACHE, key = "#result.id")
            }
    )
    public PaymentDto payment(OrderDto order) {
        BigDecimal productsCost = calculateProductCost(order);

        if (order.getDeliveryPrice() == null) {
            throw new NotEnoughInfoInOrderToCalculateException("Нет информации о доставке");
        }

        BigDecimal taxCost = productsCost.multiply(TAX_RATE);
        BigDecimal totalCost = productsCost.add(taxCost).add(order.getDeliveryPrice());

        Payment payment = Payment.builder()
                .orderId(order.getOrderId())
                .productCost(productsCost)
                .deliveryCost(order.getDeliveryPrice())
                .taxCost(taxCost)
                .totalCost(totalCost)
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        payment = repository.save(payment);

        return PaymentMapper.toDto(payment);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = PAYMENTS_CACHE, key = "#paymentId", unless = "#result == null")
    public PaymentDto getPayment(UUID paymentId) {
        Payment payment = repository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
        return PaymentMapper.toDto(payment);
    }

    @Override
    @CachePut(value = PAYMENTS_CACHE, key = "#paymentId")
    @CacheEvict(value = TRANSACTION_STATUS_CACHE, allEntries = true)
    public PaymentDto markPaymentSuccess(UUID paymentId) {
        Payment payment = repository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        repository.save(payment);

        // Сообщаем order-service что оплата прошла
        orderClient.payment(payment.getOrderId());

        return PaymentMapper.toDto(payment);
    }

    @Override
    @CachePut(value = PAYMENTS_CACHE, key = "#paymentId")
    @CacheEvict(value = TRANSACTION_STATUS_CACHE, allEntries = true)
    public PaymentDto markPaymentFailed(UUID paymentId) {
        Payment payment = repository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
        payment.setPaymentStatus(PaymentStatus.FAILED);
        repository.save(payment);
        orderClient.paymentFailed(payment.getOrderId());
        return PaymentMapper.toDto(payment);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = PAYMENTS_CACHE, key = "'order:' + #orderId", unless = "#result == null")
    public PaymentDto getPaymentByOrderId(UUID orderId) {
        Payment payment = repository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException(orderId));
        return PaymentMapper.toDto(payment);
    }
}