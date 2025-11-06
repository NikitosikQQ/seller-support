package ru.seller_support.assignment.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import ru.seller_support.assignment.domain.enums.Marketplace;

import java.math.BigDecimal;

@Data
@Builder
@Jacksonized
@AllArgsConstructor
public class AveragePricePerMeterLdsp16Model {
    private BigDecimal averagePrice;
    private Marketplace marketplace;
}
