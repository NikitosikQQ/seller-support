package ru.seller_support.assignment.adapter.postgres.entity.employee;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.seller_support.assignment.domain.enums.Workplace;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "employees_processed_capacity")
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
public class EmployeeProcessedCapacityEntity {

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

    @Column(name = "earned_amount")
    private BigDecimal earnedAmount;

    @Column(name = "processed_at")
    private LocalDate processedAt;
}
