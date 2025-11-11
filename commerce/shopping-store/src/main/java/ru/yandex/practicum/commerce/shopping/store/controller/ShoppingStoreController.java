package ru.yandex.practicum.commerce.shopping.store.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.interaction.client.shopping.store.ShoppingStoreOperations;
import ru.yandex.practicum.commerce.interaction.dto.shopping.store.ProductCategory;
import ru.yandex.practicum.commerce.interaction.dto.shopping.store.ProductDto;
import ru.yandex.practicum.commerce.interaction.dto.shopping.store.QuantityState;
import ru.yandex.practicum.commerce.interaction.dto.shopping.store.SetProductQuantityStateRequest;
import ru.yandex.practicum.commerce.shopping.store.service.ShoppingStoreService;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/shopping-store")
@RequiredArgsConstructor
@Validated
public class ShoppingStoreController implements ShoppingStoreOperations {
    private final ShoppingStoreService shoppingStoreService;

    @Override
    @GetMapping
    public Page<ProductDto> getProducts(
            @RequestParam ProductCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort) {

        return shoppingStoreService.getProducts(category, page, size, sort);
    }

    @Override
    @PutMapping
    public ProductDto createNewProduct(@RequestBody ProductDto productDto) {
        return shoppingStoreService.createProduct(productDto);
    }

    @Override
    @PostMapping
    public ProductDto updateProduct(@RequestBody ProductDto productDto) {
        return shoppingStoreService.updateProduct(productDto);
    }

    @Override
    @PostMapping("/removeProductFromStore")
    public boolean removeProductFromStore(@RequestBody UUID productId) {
        return shoppingStoreService.removeProductFromStore(productId);
    }

    @Override
    @PostMapping("/quantityState")
    public boolean setProductQuantityState(
            @RequestParam UUID productId,
            @RequestParam QuantityState quantityState) {
        SetProductQuantityStateRequest request = SetProductQuantityStateRequest.builder()
                .productId(productId)
                .quantityState(quantityState)
                .build();
        return shoppingStoreService.setProductQuantityState(request);
    }

    @Override
    @GetMapping("/{productId}")
    public ProductDto getProduct(@PathVariable UUID productId) {
        return shoppingStoreService.getProduct(productId);
    }
}
