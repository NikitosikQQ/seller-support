package ru.seller_support.assignment.adapter.postgres.entity.employee;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.seller_support.assignment.domain.enums.CapacityOperationType;
import ru.seller_support.assignment.domain.enums.Workplace;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "employees_activity_history")
@Getter
@Setter
@Accessors(chain = true)
public class EmployeeActivityHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column
    private UUID id;

    @Column
    private String username;

    @Column
    private BigDecimal capacity;

    @Column
    @Enumerated(EnumType.STRING)
    private Workplace workplace;

    @Column(name = "operation_type")
    @Enumerated(EnumType.STRING)
    private CapacityOperationType operationType;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "processed_at")
    private LocalDate processedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
