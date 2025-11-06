package ru.seller_support.assignment.controller.dto.response;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import ru.seller_support.assignment.domain.enums.Workplace;

import java.math.BigDecimal;

@Value
@Builder
@Jacksonized
public class EmployeeCapacityDto {

    String username;
    Workplace workplace;
    BigDecimal capacity;
    BigDecimal earnedAmount;
}
