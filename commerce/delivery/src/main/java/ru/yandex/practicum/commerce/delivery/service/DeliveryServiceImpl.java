package ru.yandex.practicum.commerce.delivery.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.delivery.dal.DeliveryRepository;
import ru.yandex.practicum.commerce.delivery.exception.DeliveryAlreadyExistsException;
import ru.yandex.practicum.commerce.delivery.exception.DeliveryNotFoundException;
import ru.yandex.practicum.commerce.delivery.exception.InvalidDeliveryStateException;
import ru.yandex.practicum.commerce.delivery.mapper.DeliveryMapper;
import ru.yandex.practicum.commerce.delivery.model.Delivery;
import ru.yandex.practicum.commerce.interaction.client.order.OrderClient;
import ru.yandex.practicum.commerce.interaction.dto.delivery.DeliveryDto;
import ru.yandex.practicum.commerce.interaction.dto.delivery.DeliveryState;
import ru.yandex.practicum.commerce.interaction.dto.order.OrderDto;
import ru.yandex.practicum.commerce.interaction.dto.order.OrderState;
import ru.yandex.practicum.commerce.interaction.dto.order.UpdateOrderStateRequest;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.AddressDto;
import ru.yandex.practicum.commerce.interaction.exception.OrderServiceException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryMapper deliveryMapper;
    private final OrderClient orderClient;

    private static final BigDecimal BASE_COST = BigDecimal.valueOf(5.0);
    private static final BigDecimal FRAGILE_MULTIPLIER = BigDecimal.valueOf(0.2);
    private static final BigDecimal WEIGHT_MULTIPLIER = BigDecimal.valueOf(0.3);
    private static final BigDecimal VOLUME_MULTIPLIER = BigDecimal.valueOf(0.2);
    private static final BigDecimal ADDRESS_MULTIPLIER = BigDecimal.valueOf(0.2);

    @Override
    public DeliveryDto createDelivery(DeliveryDto deliveryDto) {
        log.info("Creating delivery for order: {}", deliveryDto.getOrderId());

        // Проверяем, не существует ли уже доставка для этого заказа
        if (deliveryRepository.existsByOrderId(deliveryDto.getOrderId())) {
            throw new DeliveryAlreadyExistsException(deliveryDto.getOrderId());
        }

        // Генерируем ID если не указан
        if (deliveryDto.getDeliveryId() == null) {
            deliveryDto.setDeliveryId(UUID.randomUUID());
        }

        // Рассчитываем стоимость доставки
        BigDecimal cost = calculateCost(
                deliveryDto.getFromAddress(),
                deliveryDto.getToAddress(),
                deliveryDto.getTotalWeight(),
                deliveryDto.getTotalVolume(),
                deliveryDto.getFragile()
        );
        deliveryDto.setDeliveryCost(cost);

        // Устанавливаем статус CREATED если не установлен
        if (deliveryDto.getDeliveryState() == null) {
            deliveryDto.setDeliveryState(DeliveryState.CREATED);
        }

        // Сохраняем время создания
        LocalDateTime now = LocalDateTime.now();
        deliveryDto.setCreatedAt(now);
        deliveryDto.setUpdatedAt(now);

        // Сохраняем в БД
        Delivery delivery = deliveryMapper.toEntity(deliveryDto);
        Delivery savedDelivery = deliveryRepository.save(delivery);

        log.info("Created delivery: {} for order: {}", savedDelivery.getDeliveryId(), deliveryDto.getOrderId());
        return deliveryMapper.toDto(savedDelivery);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateDeliveryCost(OrderDto orderDto) {
        log.info("Calculating delivery cost for order: {}", orderDto.getOrderId());

        Double totalWeight = orderDto.getDeliveryWeight();
        Double totalVolume = orderDto.getDeliveryVolume();
        Boolean fragile = orderDto.getFragile();

        // Адрес склада (из конфигурации или константы)
        AddressDto warehouseAddress = AddressDto.builder()
                .country("Россия")
                .city("Москва")
                .street("ADDRESS_2")
                .house("1")
                .flat("1")
                .build();

        // Адрес доставки (в реальном приложении получаем из заказа)
        AddressDto deliveryAddress = AddressDto.builder()
                .country("Россия")
                .city("Москва")
                .street("Пролетарская")
                .house("31")
                .flat("10")
                .build();

        BigDecimal cost = calculateCost(warehouseAddress, deliveryAddress, totalWeight, totalVolume, fragile);

        log.info("Calculated delivery cost {} for order: {}", cost, orderDto.getOrderId());
        return cost;
    }

    @Override
    @Retryable(value = {FeignException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void processDeliveryPicked(UUID orderId) {
        log.info("Processing delivery picked for order: {}", orderId);

        Delivery delivery = getDeliveryEntityByOrderId(orderId);

        if (delivery.getDeliveryState() != DeliveryState.CREATED) {
            throw new InvalidDeliveryStateException(
                    "Delivery must be in CREATED state. Current state: " + delivery.getDeliveryState()
            );
        }

        delivery.setDeliveryState(DeliveryState.IN_PROGRESS);
        deliveryRepository.save(delivery);

        // Уведомляем сервис заказов
        try {
            orderClient.updateOrderState(orderId, new UpdateOrderStateRequest(OrderState.ASSEMBLED));
            log.info("Updated order {} state to ASSEMBLED", orderId);
        } catch (Exception e) {
            log.error("Failed to update order state for order: {}", orderId, e);
            throw new OrderServiceException("Failed to update order state", e);
        }

        log.info("Delivery for order {} processed as PICKED", orderId);
    }

    @Override
    @Retryable(value = {FeignException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void processDeliverySuccess(UUID orderId) {
        log.info("Processing delivery success for order: {}", orderId);

        Delivery delivery = getDeliveryEntityByOrderId(orderId);

        if (delivery.getDeliveryState() != DeliveryState.IN_PROGRESS) {
            throw new InvalidDeliveryStateException(
                    "Delivery must be in IN_PROGRESS state. Current state: " + delivery.getDeliveryState()
            );
        }

        delivery.setDeliveryState(DeliveryState.DELIVERED);
        deliveryRepository.save(delivery);

        // Уведомляем сервис заказов
        try {
            orderClient.updateOrderState(orderId, new UpdateOrderStateRequest(OrderState.DELIVERED));
            log.info("Updated order {} state to DELIVERED", orderId);
        } catch (Exception e) {
            log.error("Failed to update order state for order: {}", orderId, e);
            // Можно добавить компенсирующую транзакцию
        }

        log.info("Delivery for order {} processed as SUCCESSFUL", orderId);
    }

    @Override
    @Retryable(value = {FeignException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void processDeliveryFailed(UUID orderId) {
        log.info("Processing delivery failure for order: {}", orderId);

        Delivery delivery = getDeliveryEntityByOrderId(orderId);

        if (delivery.getDeliveryState() == DeliveryState.DELIVERED ||
                delivery.getDeliveryState() == DeliveryState.CANCELLED) {
            throw new InvalidDeliveryStateException(
                    "Cannot mark as FAILED. Current state: " + delivery.getDeliveryState()
            );
        }

        delivery.setDeliveryState(DeliveryState.FAILED);
        deliveryRepository.save(delivery);

        // Уведомляем сервис заказов
        try {
            orderClient.updateOrderState(orderId, new UpdateOrderStateRequest(OrderState.DELIVERY_FAILED));
            log.info("Updated order {} state to DELIVERY_FAILED", orderId);
        } catch (Exception e) {
            log.error("Failed to update order state for order: {}", orderId, e);
        }

        log.info("Delivery for order {} processed as FAILED", orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryDto getDeliveryById(UUID deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new DeliveryNotFoundException(deliveryId));
        return deliveryMapper.toDto(delivery);
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryDto getDeliveryByOrderId(UUID orderId) {
        Delivery delivery = getDeliveryEntityByOrderId(orderId);
        return deliveryMapper.toDto(delivery);
    }

    // ========== Вспомогательные методы ==========

    private Delivery getDeliveryEntityByOrderId(UUID orderId) {
        return deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new DeliveryNotFoundException("Delivery not found for order: " + orderId));
    }

    private BigDecimal calculateCost(AddressDto fromAddress, AddressDto toAddress,
                                     Double weight, Double volume, Boolean fragile) {
        BigDecimal cost = BASE_COST;

        // 1. Учитываем адрес склада
        String warehouseStreet = fromAddress.getStreet();
        if (warehouseStreet != null) {
            if (warehouseStreet.contains("ADDRESS_1")) {
                cost = cost.multiply(BigDecimal.ONE);
            } else if (warehouseStreet.contains("ADDRESS_2")) {
                cost = cost.multiply(BigDecimal.valueOf(2));
            }
        }
        cost = cost.add(BASE_COST);

        // 2. Учитываем хрупкость
        if (fragile != null && fragile) {
            BigDecimal fragileCost = cost.multiply(FRAGILE_MULTIPLIER);
            cost = cost.add(fragileCost);
        }

        // 3. Учитываем вес
        if (weight != null) {
            BigDecimal weightCost = BigDecimal.valueOf(weight).multiply(WEIGHT_MULTIPLIER);
            cost = cost.add(weightCost);
        }

        // 4. Учитываем объем
        if (volume != null) {
            BigDecimal volumeCost = BigDecimal.valueOf(volume).multiply(VOLUME_MULTIPLIER);
            cost = cost.add(volumeCost);
        }

        // 5. Учитываем адрес доставки
        if (fromAddress.getStreet() != null && toAddress.getStreet() != null) {
            if (!fromAddress.getStreet().equals(toAddress.getStreet())) {
                BigDecimal addressCost = cost.multiply(ADDRESS_MULTIPLIER);
                cost = cost.add(addressCost);
            }
        }

        return cost.setScale(2, RoundingMode.HALF_UP);
    }
}