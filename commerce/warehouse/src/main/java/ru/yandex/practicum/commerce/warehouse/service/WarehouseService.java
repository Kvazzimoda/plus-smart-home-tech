package ru.yandex.practicum.commerce.warehouse.service;

import ru.yandex.practicum.commerce.interaction.dto.shopping.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.AddressDto;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.NewProductInWarehouseRequest;

public interface WarehouseService {

    void newProductInWarehouse(NewProductInWarehouseRequest request);

    BookedProductsDto checkAvailability(ShoppingCartDto shoppingCartDto);

    void addProductToWarehouse(AddProductToWarehouseRequest request);

    AddressDto getWarehouseAddress();

}
