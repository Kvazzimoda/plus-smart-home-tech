package ru.yandex.practicum.commerce.warehouse.exception;

import lombok.Getter;

import java.util.UUID;

@Getter
public class SpecifiedProductAlreadyInWarehouseBusinessException extends RuntimeException {

    private final UUID productId;

    public SpecifiedProductAlreadyInWarehouseBusinessException(UUID productId) {
        super("Product already in warehouse: " + productId);
        this.productId = productId;
    }

    public SpecifiedProductAlreadyInWarehouseBusinessException(UUID productId, String message) {
        super(message);
        this.productId = productId;
    }

}
