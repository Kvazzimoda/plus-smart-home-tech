package ru.yandex.practicum.commerce.warehouse.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.interaction.client.warehouse.WarehouseOperations;
import ru.yandex.practicum.commerce.interaction.dto.shopping.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.AddressDto;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.NewProductInWarehouseRequest;
import ru.yandex.practicum.commerce.warehouse.service.WarehouseService;

@Slf4j
@RestController
@RequestMapping("/api/v1/warehouse")
@RequiredArgsConstructor
@Validated
public class WarehouseController implements WarehouseOperations {

    private final WarehouseService warehouseService;

    @Override
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PutMapping
    public void newProductInWarehouse(@Valid @RequestBody NewProductInWarehouseRequest request) {
        log.info("Adding new product to warehouse: productId={}", request.getProductId());
        warehouseService.newProductInWarehouse(request);
    }

    @Override
    @PostMapping("/check")
    public BookedProductsDto checkAvailability(
            @Valid @RequestBody ShoppingCartDto shoppingCartDto) {
        log.info("Checking quantity for {} products in cart", shoppingCartDto);
        return warehouseService.checkAvailability(shoppingCartDto);
    }

    @Override
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/add")
    public void addProductToWarehouse(@Valid @RequestBody AddProductToWarehouseRequest request) {
        log.info("Increasing stock: productId={}, quantity={}", request.getProductId(), request.getQuantity());
        warehouseService.addProductToWarehouse(request);
    }

    @Override
    @GetMapping("/address")
    public AddressDto getWarehouseAddress() {
        log.info("Fetching warehouse address");
        return warehouseService.getWarehouseAddress();
    }
}

