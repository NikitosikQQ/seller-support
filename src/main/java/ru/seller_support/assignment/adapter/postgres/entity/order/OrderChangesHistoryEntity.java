package ru.seller_support.assignment.adapter.postgres.entity.order;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import ru.seller_support.assignment.domain.enums.OrderStatus;
import ru.seller_support.assignment.domain.enums.Workplace;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "orders_changes_history")
@Getter
@Setter
@Accessors(chain = true)
public class OrderChangesHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column
    private UUID id;

    @Column(name = "order_number")
    private String orderNumber;

    @Column
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column
    private String author;

    @Column
    @Enumerated(EnumType.STRING)
    private Workplace workplace;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

}
