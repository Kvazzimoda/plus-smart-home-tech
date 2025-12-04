package ru.yandex.practicum.commerce.payment.dal;

import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.commerce.interaction.dto.payment.PaymentStatus;
import ru.yandex.practicum.commerce.payment.model.Payment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    // Ищет платеж по ID заказа
    Optional<Payment> findByOrderId(UUID orderId);

    // Возвращает все платежи с указанным статусом
    List<Payment> findByPaymentStatus(PaymentStatus status);

    // Более сложный поиск по заказу и статусу одновременно
    @Query("SELECT p FROM Payment p WHERE p.orderId = :orderId AND p.paymentStatus = :status")
    Optional<Payment> findByOrderIdAndPaymentStatus(@Param("orderId") UUID orderId,
                                                    @Param("status") PaymentStatus status);

    // Проверяет, существует ли платеж с конкретным ID и статусом
    boolean existsByPaymentIdAndPaymentStatus(UUID paymentId, PaymentStatus status);
}

