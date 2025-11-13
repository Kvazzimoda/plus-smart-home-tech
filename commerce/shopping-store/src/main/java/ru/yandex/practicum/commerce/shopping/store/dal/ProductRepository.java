package ru.yandex.practicum.commerce.shopping.store.dal;

import ru.yandex.practicum.commerce.shopping.store.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.commerce.interaction.dto.shopping.store.ProductCategory;
import ru.yandex.practicum.commerce.interaction.dto.shopping.store.ProductState;

import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    Page<Product> findByProductCategoryAndProductState(
            ProductCategory productCategory,
            ProductState productState,
            Pageable pageable);

    Page<Product> findByProductCategory(
            ProductCategory productCategory,
            Pageable pageable);

    boolean existsByProductIdAndProductState(UUID productId, ProductState productState);
}
