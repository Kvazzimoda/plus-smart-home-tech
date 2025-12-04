package ru.yandex.practicum.commerce.shopping.store.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.commerce.shopping.store.model.Product;
import ru.yandex.practicum.commerce.interaction.dto.shopping.store.ProductDto;
import ru.yandex.practicum.commerce.interaction.dto.shopping.store.ProductState;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductMapper {

    /**
     * Конвертирует Entity в DTO
     */
    public static ProductDto toDto(Product entity) {
        if (entity == null) {
            return null;
        }

        return ProductDto.builder()
                .productId(entity.getProductId())
                .productName(entity.getProductName())
                .description(entity.getDescription())
                .imageSrc(entity.getImageSrc())
                .quantityState(entity.getQuantityState())
                .productState(entity.getProductState())
                .productCategory(entity.getProductCategory())
                .price(entity.getPrice())
                .build();
    }

    /**
     * Конвертирует DTO в Entity (для создания нового продукта)
     */
    public static Product toEntity(ProductDto dto) {
        if (dto == null) {
            return null;
        }

        return Product.builder()
                .productId(dto.getProductId()) // Будет null для новых продуктов, сгенерируется в БД
                .productName(dto.getProductName())
                .description(dto.getDescription())
                .imageSrc(dto.getImageSrc())
                .quantityState(dto.getQuantityState())
                .productState(dto.getProductState() != null ? dto.getProductState() : ProductState.ACTIVE)
                .productCategory(dto.getProductCategory())
                .price(dto.getPrice())
                .build();
    }

    /**
     * Обновляет существующий Entity из DTO (для обновления продукта)
     */
    public static void updateEntityFromDto(Product entity, ProductDto dto) {
        if (entity == null || dto == null) {
            return;
        }

        entity.setProductName(dto.getProductName());
        entity.setDescription(dto.getDescription());
        entity.setImageSrc(dto.getImageSrc());
        entity.setQuantityState(dto.getQuantityState());
        entity.setProductCategory(dto.getProductCategory());
        entity.setPrice(dto.getPrice());
        // productState не обновляем через update - для этого есть отдельный метод
    }

    /**
     * Создает новый Entity с минимальными обязательными полями
     */
    public static Product toNewEntity(String productName, String description,
                                      String productCategory, String price) {
        return Product.builder()
                .productName(productName)
                .description(description)
                .productState(ProductState.ACTIVE)
                // остальные поля могут быть установлены позже
                .build();
    }
}
