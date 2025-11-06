package ru.seller_support.assignment.adapter.postgres.entity.workplace;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Entity
@Table(name = "workplace_material_rate")
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class WorkplaceMaterialRatesEntity {

    @EmbeddedId
    private WorkplaceMaterialRateId id;

    @Column(nullable = false, scale = 2)
    private BigDecimal coefficient;
}
