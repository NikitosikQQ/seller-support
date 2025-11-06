package ru.seller_support.assignment.adapter.postgres.entity.workplace;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.seller_support.assignment.domain.enums.Workplace;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class WorkplaceMaterialRateId implements Serializable {

    @Enumerated(EnumType.STRING)
    @Column(name = "workplace", nullable = false)
    private Workplace workplace;

    @Column(name = "material_name", nullable = false)
    private String materialName;

}
