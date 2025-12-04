package ru.yandex.practicum.commerce.warehouse.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import ru.yandex.practicum.commerce.interaction.dto.delivery.ShippedToDeliveryRequest;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.*;
import ru.yandex.practicum.commerce.warehouse.dal.OrderBookingRepository;
import ru.yandex.practicum.commerce.warehouse.dal.WarehouseProductRepository;
import ru.yandex.practicum.commerce.warehouse.exception.NoSpecifiedProductInWarehouseBusinessException;
import ru.yandex.practicum.commerce.warehouse.exception.ProductInShoppingCartLowQuantityBusinessException;
import ru.yandex.practicum.commerce.warehouse.exception.ProductInShoppingCartNotInWarehouseBusinessException;
import ru.yandex.practicum.commerce.warehouse.exception.SpecifiedProductAlreadyInWarehouseBusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.commerce.warehouse.mapper.WarehouseMapper;
import ru.yandex.practicum.commerce.warehouse.model.OrderBookingEntity;
import ru.yandex.practicum.commerce.warehouse.model.WarehouseProductEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.interaction.dto.shopping.cart.ShoppingCartDto;

import java.security.SecureRandom;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseProductRepository warehouseProductRepository;
    private final OrderBookingRepository orderBookingRepository;

    private static final String[] ADDRESSES = new String[]{"ADDRESS_1", "ADDRESS_2"};
    private static final String CURRENT_ADDRESS = ADDRESSES[Random.from(new SecureRandom()).nextInt(0, ADDRESSES.length)];

    // Константы для имен кешей
    private static final String PRODUCT_CACHE = "products";
    private static final String ADDRESS_CACHE = "addresses";
    private static final String BOOKING_CACHE = "reservations";
    private static final String WAREHOUSE_ITEMS_CACHE = "warehouse-items";

    @Override
    @Transactional
    @CacheEvict(value = {PRODUCT_CACHE, WAREHOUSE_ITEMS_CACHE}, allEntries = true)
    public void newProductInWarehouse(NewProductInWarehouseRequest request) {
        log.debug("Adding new product to warehouse: {}", request.getProductId());

        if (warehouseProductRepository.existsByProductId(request.getProductId())) {
            throw new SpecifiedProductAlreadyInWarehouseBusinessException(request.getProductId());
        }

        WarehouseProductEntity entity = WarehouseMapper.toEntity(request);
        warehouseProductRepository.save(entity);

        log.info("New product added to warehouse: {}", request.getProductId());
    }

    @Override
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto shoppingCartDto) {
        log.debug("Checking product quantity for shopping cart: {}", shoppingCartDto.getShoppingCartId());

        Map<UUID, Integer> insufficientProducts = new HashMap<>();
        double totalWeight = 0.0;
        double totalVolume = 0.0;
        boolean hasFragile = false;

        for (Map.Entry<UUID, Long> entry : shoppingCartDto.getProducts().entrySet()) {
            UUID productId = entry.getKey();
            Long requiredQuantity = entry.getValue();

            WarehouseProductEntity product = warehouseProductRepository.findByProductId(productId)
                    .orElseThrow(() -> new NoSpecifiedProductInWarehouseBusinessException(productId));

            if (product.getQuantity() < requiredQuantity) {
                insufficientProducts.put(productId, (int) (requiredQuantity - product.getQuantity().intValue()));
            }

            // Рассчитываем общие характеристики
            if (product.getWeight() != null) {
                totalWeight += product.getWeight() * requiredQuantity;
            }
            if (product.getWidth() != null && product.getHeight() != null && product.getDepth() != null) {
                totalVolume += product.getWidth() * product.getHeight() * product.getDepth() * requiredQuantity;
            }
            if (Boolean.TRUE.equals(product.getFragile())) {
                hasFragile = true;
            }
        }

        if (!insufficientProducts.isEmpty()) {
            throw new ProductInShoppingCartLowQuantityBusinessException(insufficientProducts);
        }

        BookedProductsDto result = BookedProductsDto.builder()
                .deliveryWeight(totalWeight)
                .deliveryVolume(totalVolume)
                .fragile(hasFragile)
                .build();

        log.debug("Product quantity check completed for cart: {}, result: {}", shoppingCartDto.getShoppingCartId(), result);
        return result;
    }

    @Override
    @Transactional
    @CachePut(value = PRODUCT_CACHE, key = "#request.productId")
    @CacheEvict(value = WAREHOUSE_ITEMS_CACHE, allEntries = true)
    public void addProductToWarehouse(AddProductToWarehouseRequest request) {
        log.debug("Adding product quantity to warehouse: {}", request.getProductId());

        WarehouseProductEntity product = warehouseProductRepository.findByProductId(request.getProductId())
                .orElseThrow(() -> new NoSpecifiedProductInWarehouseBusinessException(request.getProductId()));

        product.setQuantity(product.getQuantity() + request.getQuantity());
        warehouseProductRepository.save(product);

        log.info("Product quantity updated for: {}, new quantity: {}", request.getProductId(), product.getQuantity());
    }

    @Override
    @Cacheable(value = ADDRESS_CACHE, key = "'current'")
    public AddressDto getWarehouseAddress() {
        log.debug("Getting warehouse address: {}", CURRENT_ADDRESS);

        // Дублируем адрес во все поля
        return AddressDto.builder()
                .country(CURRENT_ADDRESS)
                .city(CURRENT_ADDRESS)
                .street(CURRENT_ADDRESS)
                .house(CURRENT_ADDRESS)
                .flat(CURRENT_ADDRESS)
                .build();
    }

    @Override
    @Transactional
    @Cacheable(value = BOOKING_CACHE, key = "#request.orderId", unless = "#result == null")
    public BookedProductsDto assemblyProductsForOrder(AssemblyProductsForOrderRequest request) {
        log.info("Assembling products for order: {}", request.getOrderId());

        Map<UUID, Integer> insufficientProducts = new HashMap<>();
        double totalWeight = 0.0;
        double totalVolume = 0.0;
        boolean hasFragile = false;

        // Проверяем наличие и резервируем товары
        for (Map.Entry<UUID, Long> entry : request.getProducts().entrySet()) {
            UUID productId = entry.getKey();
            Long requiredQuantity = entry.getValue();

            WarehouseProductEntity product = warehouseProductRepository.findByProductId(productId)
                    .orElseThrow(() -> new ProductInShoppingCartNotInWarehouseBusinessException(productId));

            // Проверяем доступное количество (учитывая уже забронированные)
            Long totalBooked = orderBookingRepository.getTotalBookedQuantityByProductId(productId);
            long availableQuantity = product.getQuantity() - (totalBooked != null ? totalBooked : 0L);

            if (availableQuantity < requiredQuantity) {
                insufficientProducts.put(productId, (int) (requiredQuantity - (int) availableQuantity));
                continue;
            }

            // Создаем или обновляем бронирование
            OrderBookingEntity booking = orderBookingRepository.findByOrderIdAndProductId(request.getOrderId(), productId)
                    .orElse(OrderBookingEntity.builder()
                            .orderId(request.getOrderId())
                            .productId(productId)
                            .quantity(0)
                            .build());

            booking.setQuantity((int) (booking.getQuantity() + requiredQuantity));
            orderBookingRepository.save(booking);

            // Рассчитываем характеристики
            if (product.getWeight() != null) {
                totalWeight += product.getWeight() * requiredQuantity;
            }
            if (product.getWidth() != null && product.getHeight() != null && product.getDepth() != null) {
                totalVolume += product.getWidth() * product.getHeight() * product.getDepth() * requiredQuantity;
            }
            if (Boolean.TRUE.equals(product.getFragile())) {
                hasFragile = true;
            }
        }

        if (!insufficientProducts.isEmpty()) {
            throw new ProductInShoppingCartLowQuantityBusinessException(insufficientProducts);
        }

        BookedProductsDto result = BookedProductsDto.builder()
                .deliveryWeight(totalWeight)
                .deliveryVolume(totalVolume)
                .fragile(hasFragile)
                .build();

        log.info("Products assembled for order: {}, result: {}", request.getOrderId(), result);
        return result;
    }

    @Override
    @Transactional
    @CacheEvict(value = {PRODUCT_CACHE, BOOKING_CACHE}, key = "#request.orderId")
    public void shippedToDelivery(ShippedToDeliveryRequest request) {
        log.info("Shipping products to delivery for order: {}, delivery: {}",
                request.getOrderId(), request.getDeliveryId());

        // Обновляем бронирования, добавляя deliveryId
        orderBookingRepository.updateDeliveryIdByOrderId(request.getOrderId(), request.getDeliveryId());

        // Уменьшаем количество товаров на складе
        List<OrderBookingEntity> bookings = orderBookingRepository.findByOrderId(request.getOrderId());

        for (OrderBookingEntity booking : bookings) {
            WarehouseProductEntity product = warehouseProductRepository.findByProductId(booking.getProductId())
                    .orElseThrow(() -> new NoSpecifiedProductInWarehouseBusinessException(booking.getProductId()));

            if (product.getQuantity() < booking.getQuantity()) {
                throw new ProductInShoppingCartLowQuantityBusinessException(
                        Map.of(booking.getProductId(), booking.getQuantity() - product.getQuantity().intValue()));
            }

            product.setQuantity(product.getQuantity() - booking.getQuantity());
            warehouseProductRepository.save(product);
        }

        log.info("Products shipped to delivery for order: {}", request.getOrderId());
    }

    @Override
    @Transactional
    @CacheEvict(value = PRODUCT_CACHE, allEntries = true)
    public void acceptReturn(Map<UUID, Long> returnedProducts) {
        log.info("Accepting product returns: {}", returnedProducts);

        for (Map.Entry<UUID, Long> entry : returnedProducts.entrySet()) {
            UUID productId = entry.getKey();
            Long quantity = entry.getValue();

            WarehouseProductEntity product = warehouseProductRepository.findByProductId(productId)
                    .orElseThrow(() -> new NoSpecifiedProductInWarehouseBusinessException(productId));

            product.setQuantity(product.getQuantity() + quantity);
            warehouseProductRepository.save(product);
        }

        log.info("Product returns accepted: {}", returnedProducts);
    }
}