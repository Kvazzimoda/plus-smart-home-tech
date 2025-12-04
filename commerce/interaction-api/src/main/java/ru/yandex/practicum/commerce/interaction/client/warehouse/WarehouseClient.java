package ru.yandex.practicum.commerce.interaction.client.warehouse;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.commerce.interaction.dto.delivery.ShippedToDeliveryRequest;
import ru.yandex.practicum.commerce.interaction.dto.shopping.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.interaction.dto.warehouse.*;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "warehouse", fallback = WarehouseClientFallback.class)
public interface WarehouseClient extends WarehouseOperations {

    @PostMapping("/check")
    @Override
    BookedProductsDto checkProductQuantityEnoughForShoppingCart(@RequestBody ShoppingCartDto shoppingCartDto);

    @PostMapping("/assembly")
    @Override
    BookedProductsDto assemblyProductsForOrder(@RequestBody AssemblyProductsForOrderRequest request);

    @GetMapping("/address")
    @Override
    AddressDto getWarehouseAddress();

    @PostMapping("/return")
    @Override
    void acceptReturn(@RequestBody Map<UUID, Long> returnedProducts);

    @PostMapping("/product")
    @Override
    void newProductInWarehouse(@RequestBody NewProductInWarehouseRequest request);

    @PutMapping("/product")
    @Override
    void addProductToWarehouse(@RequestBody AddProductToWarehouseRequest request);

    @PostMapping("/shipped-to-delivery")
    @Override
    void shippedToDelivery(@RequestBody ShippedToDeliveryRequest request);
}
