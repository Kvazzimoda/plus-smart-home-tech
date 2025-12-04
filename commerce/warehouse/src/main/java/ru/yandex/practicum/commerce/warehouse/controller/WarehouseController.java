package ru.yandex.practicum.commerce.warehouse.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.interaction.client.warehouse.WarehouseOperations;
import ru.yandex.practicum.commerce.interaction.dto.delivery.ShippedToDeliveryRequest;
import ru.yandex.practicum.commerce.interaction.dto.shopping.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.*;
import ru.yandex.practicum.commerce.warehouse.service.WarehouseService;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/warehouse")
@RequiredArgsConstructor
@Validated
public class WarehouseController implements WarehouseOperations {

    private final WarehouseService warehouseService;

    @Override
    @PutMapping
    public void newProductInWarehouse(@RequestBody NewProductInWarehouseRequest request) {
        log.debug("Adding new product to warehouse: {}", request);
        warehouseService.newProductInWarehouse(request);
        log.debug("New product added to warehouse successfully");
    }

    @Override
    @PostMapping("/check")
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(@RequestBody ShoppingCartDto shoppingCartDto) {
        log.debug("Checking product quantity for shopping cart: {}", shoppingCartDto);
        BookedProductsDto result = warehouseService.checkProductQuantityEnoughForShoppingCart(shoppingCartDto);
        log.debug("Product quantity check completed: {}", result);
        return result;
    }

    @Override
    @PostMapping("/add")
    public void addProductToWarehouse(@RequestBody AddProductToWarehouseRequest request) {
        log.debug("Adding product quantity to warehouse: {}", request);
        warehouseService.addProductToWarehouse(request);
        log.debug("Product quantity added to warehouse successfully");
    }

    @Override
    @GetMapping("/address")
    public AddressDto getWarehouseAddress() {
        log.debug("Requesting warehouse address");
        AddressDto address = warehouseService.getWarehouseAddress();
        log.debug("Return warehouse address: {}", address);
        return address;
    }

    @Override
    @PostMapping("/assembly")
    public BookedProductsDto assemblyProductsForOrder(@RequestBody AssemblyProductsForOrderRequest request) {
        log.info("Processing assembly for order: {}", request.getOrderId());
        return warehouseService.assemblyProductsForOrder(request);
    }

    @Override
    @PostMapping("/return")
    public void acceptReturn(@RequestBody Map<UUID, Long> returnedProducts) {
        log.info("Accepting returned products: {}", returnedProducts);
        warehouseService.acceptReturn(returnedProducts);
    }

    @Override
    @PostMapping("/shipped")
    public void shippedToDelivery(@RequestBody ShippedToDeliveryRequest request) {
        log.info("Processing shipment to delivery for order: {}", request.getOrderId());
        warehouseService.shippedToDelivery(request);
    }
}
