package ru.seller_support.assignment.adapter.postgres.entity.workplace;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.seller_support.assignment.domain.enums.Workplace;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "workplace_material_rate")
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class WorkplaceMaterialRatesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "workplace", nullable = false)
    private Workplace workplace;

    @Column(name = "material_name", nullable = false)
    private String materialName;

    @Column(nullable = false, scale = 2)
    private BigDecimal coefficient;

    @Column(name = "min_area_in_meters", scale = 2)
    private BigDecimal minAreaInMeters;

    @Column(name = "max_area_in_meters", scale = 2)
    private BigDecimal maxAreaInMeters;
}
