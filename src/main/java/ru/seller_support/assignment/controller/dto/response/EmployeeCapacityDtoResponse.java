package ru.seller_support.assignment.controller.dto.response;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;

@Value
@Builder
@Jacksonized
public class EmployeeCapacityDtoResponse {

    String username;
    String workplace;
    BigDecimal capacity;
    BigDecimal earnedAmount;
}
