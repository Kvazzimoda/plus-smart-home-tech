package ru.yandex.practicum.commerce.warehouse.exception;


import lombok.Getter;

import java.util.Map;
import java.util.UUID;

@Getter
public class ProductInShoppingCartLowQuantityBusinessException extends RuntimeException {

    private final Map<UUID, Integer> insufficientProducts; // productId -> missing quantity

    public ProductInShoppingCartLowQuantityBusinessException(Map<UUID, Integer> insufficientProducts) {
        super("Insufficient products in warehouse: " + insufficientProducts);
        this.insufficientProducts = insufficientProducts;
    }

    public ProductInShoppingCartLowQuantityBusinessException(Map<UUID, Integer> insufficientProducts, String message) {
        super(message);
        this.insufficientProducts = insufficientProducts;
    }

}
