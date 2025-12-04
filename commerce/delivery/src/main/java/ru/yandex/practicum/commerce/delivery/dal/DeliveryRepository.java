package ru.yandex.practicum.commerce.delivery.dal;

import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.commerce.delivery.model.Delivery;
import ru.yandex.practicum.commerce.interaction.dto.delivery.DeliveryState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {

    Optional<Delivery> findByOrderId(UUID orderId);

    List<Delivery> findByDeliveryState(DeliveryState state);

    boolean existsByOrderId(UUID orderId);

    @Query("SELECT d FROM Delivery d WHERE d.deliveryState = :state AND d.createdAt < :date")
    List<Delivery> findStaleDeliveries(@Param("state") DeliveryState state,
                                       @Param("date") LocalDateTime date);
}
