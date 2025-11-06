package ru.seller_support.assignment.controller.dto.request.employee;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class EmployeeDto {
    String username;
    String workplace;
}
