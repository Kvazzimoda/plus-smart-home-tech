package ru.yandex.practicum.commerce.delivery.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import lombok.*;
import ru.yandex.practicum.commerce.interaction.dto.delivery.DeliveryState;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "delivery")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"deliveryId"})
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "delivery_id")
    private UUID deliveryId;

    @Column(name = "order_id", nullable = false, unique = true)
    private UUID orderId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "country", column = @Column(name = "from_country")),
            @AttributeOverride(name = "city", column = @Column(name = "from_city")),
            @AttributeOverride(name = "street", column = @Column(name = "from_street")),
            @AttributeOverride(name = "house", column = @Column(name = "from_house")),
            @AttributeOverride(name = "flat", column = @Column(name = "from_flat"))
    })
    private Address fromAddress;  // Адрес склада

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "country", column = @Column(name = "to_country")),
            @AttributeOverride(name = "city", column = @Column(name = "to_city")),
            @AttributeOverride(name = "street", column = @Column(name = "to_street")),
            @AttributeOverride(name = "house", column = @Column(name = "to_house")),
            @AttributeOverride(name = "flat", column = @Column(name = "to_flat"))
    })
    private Address toAddress;    // Адрес клиента

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_state", nullable = false)
    private DeliveryState deliveryState;

    @Column(name = "total_weight")
    private Double totalWeight;

    @Column(name = "total_volume")
    private Double totalVolume;

    @Column(name = "fragile")
    private Boolean fragile = false;  // Значение по умолчанию

    @Column(name = "delivery_cost")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal deliveryCost;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (deliveryState == null) {
            deliveryState = DeliveryState.CREATED;
        }
        if (fragile == null) {
            fragile = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}