package ru.seller_support.assignment.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;

@Data
@Builder(toBuilder = true)
@Jacksonized
@AllArgsConstructor
@NoArgsConstructor
public class SummaryOfMaterialModel {
    private String materialName;
    private BigDecimal totalAreaInMeterPerDay;
    private BigDecimal totalPricePerDay;
    private BigDecimal averagePricePerSquareMeter;
}
