package ru.yandex.practicum.commerce.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.interaction.client.delivery.DeliveryClient;
import ru.yandex.practicum.commerce.interaction.client.payment.PaymentClient;
import ru.yandex.practicum.commerce.interaction.client.warehouse.WarehouseClient;
import ru.yandex.practicum.commerce.interaction.dto.delivery.DeliveryDto;
import ru.yandex.practicum.commerce.interaction.dto.delivery.DeliveryState;
import ru.yandex.practicum.commerce.interaction.dto.order.*;
import ru.yandex.practicum.commerce.interaction.dto.payment.PaymentDto;
import ru.yandex.practicum.commerce.interaction.dto.shopping.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.AddressDto;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.commerce.order.dal.OrderRepository;
import ru.yandex.practicum.commerce.order.exception.*;
import ru.yandex.practicum.commerce.order.mapper.OrderMapper;
import ru.yandex.practicum.commerce.order.model.Order;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final WarehouseClient warehouseClient;
    private final PaymentClient paymentClient;
    private final DeliveryClient deliveryClient;

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> getClientOrders(String username) {
        validateUsername(username);
        log.info("Getting orders for user: {}", username);

        return orderRepository.findByUsernameOrderByCreatedAtDesc(username).stream()
                .map(OrderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderDto createNewOrder(String username, CreateOrderRequest request) {
        log.info("Creating new order from shopping cart: {}", request.getShoppingCart().getShoppingCartId());
        validateUsername(username);

        // Проверяем доступность товаров на складе
        ShoppingCartDto tempCart = ShoppingCartDto.builder()
                .shoppingCartId(request.getShoppingCart().getShoppingCartId())
                .products(new HashMap<>(request.getShoppingCart().getProducts()))
                .build();

        BookedProductsDto bookedProductsDto;
        try {
            bookedProductsDto = warehouseClient.checkProductQuantityEnoughForShoppingCart(tempCart);
            log.debug("Products availability confirmed by warehouse");
        } catch (Exception e) {
            log.error("Failed to check product availability in warehouse: {}", e.getMessage());
            throw new ProductAvailabilityException("Product availability check failed", e);
        }

        // Создаем заказ
        Order order = Order.builder()
                .username(username)
                .shoppingCartId(request.getShoppingCart().getShoppingCartId())
                .products(new HashMap<>(request.getShoppingCart().getProducts()))
                .orderState(OrderState.NEW)
                .deliveryWeight(bookedProductsDto.getDeliveryWeight())
                .deliveryVolume(bookedProductsDto.getDeliveryVolume())
                .fragile(bookedProductsDto.getFragile())
                .country(request.getDeliveryAddress().getCountry())
                .city(request.getDeliveryAddress().getCity())
                .street(request.getDeliveryAddress().getStreet())
                .house(request.getDeliveryAddress().getHouse())
                .flat(request.getDeliveryAddress().getFlat())
                .build();

        Order savedOrder = orderRepository.save(order);

        // 0. СБОРКА ТОВАРОВ НА СКЛАДЕ
        AssemblyProductsForOrderRequest assemblyRequest = AssemblyProductsForOrderRequest.builder()
                .orderId(savedOrder.getOrderId())
                .products(savedOrder.getProducts())
                .build();

        try {
            BookedProductsDto assemblyResult = warehouseClient.assemblyProductsForOrder(assemblyRequest);
            log.debug("Products assembled for order: {}", savedOrder.getOrderId());

            // Меняем статус заказа на собран
            OrderDto assembled = assembly(savedOrder.getOrderId());

            // Обновляем характеристики доставки
            savedOrder.setDeliveryWeight(assemblyResult.getDeliveryWeight());
            savedOrder.setDeliveryVolume(assemblyResult.getDeliveryVolume());
            savedOrder.setFragile(assemblyResult.getFragile());
            savedOrder = orderRepository.save(savedOrder);

        } catch (Exception e) {
            log.error("Failed to assemble products for order {}: {}", savedOrder.getOrderId(), e.getMessage());
            OrderDto failed = assemblyFailed(savedOrder.getOrderId());
            throw new ProductAssemblyException("Product assembly failed", e);
        }

        // 1. Создаем доставку для заказа
        DeliveryDto createdDelivery;
        try {
            AddressDto warehouseAddress = warehouseClient.getWarehouseAddress();
            log.debug("Warehouse address received: {}", warehouseAddress);

            AddressDto deliveryAddress = AddressDto.builder()
                    .country(savedOrder.getCountry())
                    .city(savedOrder.getCity())
                    .street(savedOrder.getStreet())
                    .house(savedOrder.getHouse())
                    .flat(savedOrder.getFlat())
                    .build();

            DeliveryDto deliveryRequest = DeliveryDto.builder()
                    .orderId(savedOrder.getOrderId())
                    .fromAddress(warehouseAddress)
                    .toAddress(deliveryAddress)
                    .deliveryState(DeliveryState.CREATED)
                    .build();

            createdDelivery = deliveryClient.delivery(deliveryRequest);
            savedOrder.setDeliveryId(createdDelivery.getDeliveryId());
            log.debug("Delivery created with ID: {}", createdDelivery.getDeliveryId());
        } catch (Exception e) {
            log.error("Failed to create delivery: {}", e.getMessage());
            throw new DeliveryCreationException("Delivery creation failed", e);
        }

        // 2. Рассчитываем стоимость доставки
        OrderDto orderDtoForDelivery = OrderMapper.toDto(savedOrder);
        BigDecimal deliveryCost;
        try {
            deliveryCost = deliveryClient.deliveryCost(orderDtoForDelivery);
            savedOrder.setDeliveryPrice(deliveryCost);
            log.debug("Delivery cost calculated: {}", deliveryCost);
        } catch (Exception e) {
            log.error("Failed to calculate delivery cost: {}", e.getMessage());
            throw new DeliveryCostCalculationException("Delivery cost calculation failed", e);
        }

        // 3. Запускаем процесс оплаты
        OrderDto orderDtoForPayment = OrderMapper.toDto(savedOrder);
        PaymentDto paymentDto;
        try {
            paymentDto = paymentClient.payment(orderDtoForPayment);
            savedOrder.setPaymentId(paymentDto.getPaymentId());
            savedOrder.setOrderState(OrderState.ON_PAYMENT);
            savedOrder.setTotalPrice(paymentDto.getTotalCost());
            savedOrder.setProductPrice(paymentDto.getProductCost());
            log.debug("Payment process started with payment ID: {}", paymentDto.getPaymentId());
        } catch (Exception e) {
            log.error("Failed to create payment: {}", e.getMessage());
            throw new PaymentCreationException("Payment creation failed", e);
        }

        Order finalOrder = orderRepository.save(savedOrder);
        OrderDto result = OrderMapper.toDto(finalOrder);

        log.info("Created new order with id: {} and payment id: {}",
                result.getOrderId(), result.getPaymentId());

        return result;
    }

    @Override
    @Transactional
    public OrderDto productReturn(ProductReturnRequest request) {
        log.info("Processing product return for order: {}", request.getOrderId());

        Order order = getOrderEntity(request.getOrderId());
        order.setOrderState(OrderState.PRODUCT_RETURNED);

        Order updatedOrder = orderRepository.save(order);

        // Возвращаем товары обратно на склад
        try {
            warehouseClient.acceptReturn(request.getProducts());
            log.debug("All products successfully returned to warehouse: {}", request.getProducts());
        } catch (Exception e) {
            log.error("Failed to return products to warehouse: {}", e.getMessage());
            throw new ProductReturnException("Product return to warehouse failed", e);
        }

        return OrderMapper.toDto(updatedOrder);
    }

    @Override
    @Transactional
    public OrderDto payment(UUID orderId) {
        log.info("Processing payment success for order: {}", orderId);

        Order order = getOrderEntity(orderId);
        if (order.getOrderState() != OrderState.ON_PAYMENT) {
            log.warn("Order {} is in state {}, but expected ON_PAYMENT",
                    orderId, order.getOrderState());
        }

        order.setOrderState(OrderState.PAID);
        Order updatedOrder = orderRepository.save(order);

        log.info("Order {} successfully paid", orderId);

        // После успешной оплаты - вызываем доставку
        try {
            deliveryClient.deliveryPicked(orderId);
            log.debug("Delivery notified about shipment for order: {}", orderId);
        } catch (Exception e) {
            log.error("Failed to process delivery picked for order in payment service: {}. Error: {}",
                    orderId, e.getMessage());
            throw new DeliveryPickedException("Failed to process delivery picked", e);
        }

        log.info("Delivery {} successfully requested", orderId);
        return OrderMapper.toDto(updatedOrder);
    }

    @Override
    @Transactional
    public OrderDto paymentFailed(UUID orderId) {
        log.info("Processing payment failure for order: {}", orderId);

        Order order = getOrderEntity(orderId);
        order.setOrderState(OrderState.PAYMENT_FAILED);
        Order updatedOrder = orderRepository.save(order);

        log.info("Order {} payment failed", orderId);
        return OrderMapper.toDto(updatedOrder);
    }

    @Override
    @Transactional
    public OrderDto delivery(UUID orderId) {
        log.info("Processing delivery for order: {}", orderId);

        Order order = getOrderEntity(orderId);
        order.setOrderState(OrderState.DELIVERED);
        Order updatedOrder = orderRepository.save(order);

        return OrderMapper.toDto(updatedOrder);
    }

    @Override
    @Transactional
    public OrderDto deliveryFailed(UUID orderId) {
        log.info("Processing delivery failure for order: {}", orderId);

        Order order = getOrderEntity(orderId);
        order.setOrderState(OrderState.DELIVERY_FAILED);
        Order updatedOrder = orderRepository.save(order);

        return OrderMapper.toDto(updatedOrder);
    }

    @Override
    @Transactional
    public OrderDto complete(UUID orderId) {
        log.info("Completing order: {}", orderId);

        Order order = getOrderEntity(orderId);
        order.setOrderState(OrderState.COMPLETED);
        Order updatedOrder = orderRepository.save(order);

        return OrderMapper.toDto(updatedOrder);
    }

    @Override
    @Transactional
    public OrderDto calculateTotalCost(UUID orderId) {
        log.info("Calculating total cost for order: {}", orderId);

        Order order = getOrderEntity(orderId);
        OrderDto orderDto = OrderMapper.toDto(order);

        // Если стоимость доставки еще не рассчитана, рассчитываем её
        if (order.getDeliveryPrice() == null) {
            BigDecimal deliveryCost = calculateDeliveryCost(orderId).getDeliveryPrice();
            order.setDeliveryPrice(deliveryCost);
            orderDto.setDeliveryPrice(deliveryCost);
            log.debug("Delivery cost calculated during total cost calculation: {}", deliveryCost);
        }

        // Интегрируемся с payment service для расчета общей стоимости
        BigDecimal totalCost;
        try {
            totalCost = paymentClient.getTotalCost(orderDto);
            log.debug("Successfully calculated total cost from payment service: {}", totalCost);
        } catch (Exception e) {
            log.error("Failed to calculate total cost from payment service for order: {}. Error: {}",
                    orderId, e.getMessage());
            throw new TotalCostCalculationException("Failed to calculate total cost", e);
        }

        order.setTotalPrice(totalCost);
        Order updatedOrder = orderRepository.save(order);

        log.info("Total cost calculated and saved for order {}: {}", orderId, totalCost);
        return OrderMapper.toDto(updatedOrder);
    }

    @Override
    @Transactional
    public OrderDto calculateDeliveryCost(UUID orderId) {
        log.info("Calculating delivery cost for order: {}", orderId);

        Order order = getOrderEntity(orderId);
        OrderDto orderDto = OrderMapper.toDto(order);

        // Интегрируемся с delivery service для расчета стоимости доставки
        BigDecimal deliveryCost;
        try {
            deliveryCost = deliveryClient.deliveryCost(orderDto);
            log.debug("Successfully calculated delivery cost from delivery service: {}", deliveryCost);
        } catch (Exception e) {
            log.error("Failed to calculate delivery cost from delivery service for order: {}. Error: {}",
                    orderId, e.getMessage());
            throw new DeliveryCostCalculationException("Failed to calculate delivery cost", e);
        }

        order.setDeliveryPrice(deliveryCost);
        Order updatedOrder = orderRepository.save(order);

        return OrderMapper.toDto(updatedOrder);
    }

    @Override
    @Transactional
    public OrderDto assembly(UUID orderId) {
        log.info("Processing assembly for order: {}", orderId);

        Order order = getOrderEntity(orderId);
        order.setOrderState(OrderState.ASSEMBLED);
        Order updatedOrder = orderRepository.save(order);

        return OrderMapper.toDto(updatedOrder);
    }

    @Override
    @Transactional
    public OrderDto assemblyFailed(UUID orderId) {
        log.info("Processing assembly failure for order: {}", orderId);

        Order order = getOrderEntity(orderId);
        order.setOrderState(OrderState.ASSEMBLY_FAILED);
        Order updatedOrder = orderRepository.save(order);

        return OrderMapper.toDto(updatedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto getOrderById(UUID orderId) {
        Order order = getOrderEntity(orderId);
        return OrderMapper.toDto(order);
    }

    @Override
    @Transactional
    public OrderDto updateOrderState(UUID orderId, String username, OrderState newState) {
        validateUsername(username);

        Order order = orderRepository.findByOrderIdAndUsername(orderId, username)
                .orElseThrow(() -> new OrderNotFoundException(orderId,
                        "Order not found for user: " + username));

        order.setOrderState(newState);
        Order updatedOrder = orderRepository.save(order);

        return OrderMapper.toDto(updatedOrder);
    }

    // ========== Вспомогательные методы ==========

    private Order getOrderEntity(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    private void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new InvalidUsernameException("Username cannot be empty");
        }
    }
}



