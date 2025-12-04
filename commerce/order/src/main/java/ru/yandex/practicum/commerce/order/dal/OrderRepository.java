package ru.yandex.practicum.commerce.order.dal;

import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.commerce.interaction.dto.order.OrderState;
import ru.yandex.practicum.commerce.order.model.Order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByUsernameOrderByCreatedAtDesc(String username);

    Optional<Order> findByOrderIdAndUsername(UUID orderId, String username);

    List<Order> findByOrderState(OrderState orderState);

    Optional<Order> findByShoppingCartId(UUID shoppingCartId);

    boolean existsByOrderIdAndUsername(UUID orderId, String username);

    @Query("SELECT o FROM Order o WHERE o.orderState = :state AND o.createdAt < :date")
    List<Order> findStaleOrders(@Param("state") OrderState state, @Param("date") LocalDateTime date);
}

