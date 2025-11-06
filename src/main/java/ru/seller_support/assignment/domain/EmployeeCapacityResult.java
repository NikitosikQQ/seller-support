package ru.seller_support.assignment.domain;

import lombok.Builder;
import lombok.Data;
import ru.seller_support.assignment.domain.enums.Workplace;

import java.math.BigDecimal;

@Data
@Builder
public class EmployeeCapacityResult {
    private String username;
    private Workplace workplace;
    private BigDecimal totalCapacity;
    private BigDecimal totalEarnedAmount;
    private BigDecimal deltaCapacity;
    private BigDecimal deltaEarnedAmount;
}
