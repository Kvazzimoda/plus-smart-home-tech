package ru.yandex.practicum.commerce.interaction.client.shopping.store;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import ru.yandex.practicum.commerce.interaction.dto.shopping.store.ProductCategory;
import ru.yandex.practicum.commerce.interaction.dto.shopping.store.ProductDto;
import ru.yandex.practicum.commerce.interaction.dto.shopping.store.QuantityState;

import java.util.UUID;

@FeignClient(name = "shopping-store", path = "/api/v1/shopping-store")
public interface ShoppingStoreOperations {

    Page<ProductDto> getProducts(ProductCategory category, int page, int size, String sort);

    ProductDto createNewProduct(ProductDto productDto);

    ProductDto updateProduct(ProductDto productDto);

    boolean removeProductFromStore(UUID productId);

    boolean setProductQuantityState(UUID productId, QuantityState quantityState);

    ProductDto getProduct(UUID productId);
}
