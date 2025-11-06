package ru.seller_support.assignment.adapter.postgres.entity.workplace;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.seller_support.assignment.domain.enums.Workplace;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "workplace_rates")
@Getter
@Setter
public class WorkplaceRatesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column
    private UUID id;

    @Column(unique = true, nullable = false)
    @Enumerated(EnumType.STRING)
    private Workplace workplace;

    @Column(nullable = false)
    private BigDecimal rate;
}
