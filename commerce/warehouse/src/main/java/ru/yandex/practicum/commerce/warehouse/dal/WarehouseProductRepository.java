package ru.yandex.practicum.commerce.warehouse.dal;

import feign.Param;
import jakarta.persistence.LockModeType;
import ru.yandex.practicum.commerce.warehouse.model.WarehouseProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface WarehouseProductRepository extends JpaRepository<WarehouseProductEntity, UUID> {

    Optional<WarehouseProductEntity> findByProductId(UUID productId);

    List<WarehouseProductEntity> findByProductIdIn(Set<UUID> productIds);

    @Query("SELECT wp FROM WarehouseProductEntity wp WHERE wp.productId IN :productIds AND wp.quantity > 0")
    List<WarehouseProductEntity> findAvailableProductsByIds(@Param("productIds") Set<UUID> productIds);

    boolean existsByProductId(UUID productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT wp FROM WarehouseProductEntity wp WHERE wp.productId = :productId")
    Optional<WarehouseProductEntity> lockByProductId(@Param("productId") UUID productId);
}
