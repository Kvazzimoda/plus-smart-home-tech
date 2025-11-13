package ru.yandex.practicum.commerce.interaction.client.shopping.store;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.interaction.dto.shopping.store.ProductCategory;
import ru.yandex.practicum.commerce.interaction.dto.shopping.store.ProductDto;
import ru.yandex.practicum.commerce.interaction.dto.shopping.store.QuantityState;

import java.util.UUID;

@Component
public class ShoppingStoreClientFallbackFactory implements FallbackFactory<ShoppingStoreClient> {

    @Override
    public ShoppingStoreClient create(Throwable cause) {
        return new ShoppingStoreClient() {

            @Override
            public Page<ProductDto> getProducts(ProductCategory category, int page, int size, String sort) {
                throw new RuntimeException("Shopping-store service unavailable", cause);
            }

            @Override
            public ProductDto createNewProduct(ProductDto dto) {
                throw new RuntimeException("Shopping-store service unavailable", cause);
            }

            @Override
            public ProductDto updateProduct(ProductDto dto) {
                throw new RuntimeException("Shopping-store service unavailable", cause);
            }

            @Override
            public boolean removeProductFromStore(UUID productId) {
                throw new RuntimeException("Shopping-store service unavailable", cause);
            }

            @Override
            public boolean setProductQuantityState(UUID productId, QuantityState quantityState) {
                throw new RuntimeException("Shopping-store service unavailable", cause);
            }

            @Override
            public ProductDto getProduct(UUID productId) {
                throw new RuntimeException("Shopping-store service unavailable", cause);
            }
        };
    }
}
