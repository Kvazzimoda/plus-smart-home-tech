package ru.yandex.practicum.commerce.interaction.client;

@FeignClient(name = "warehouse")
public interface WarehouseClient {

    @GetMapping("/api/warehouse/{id}")
    ProductDto findProduct(@PathVariable Long id);
}