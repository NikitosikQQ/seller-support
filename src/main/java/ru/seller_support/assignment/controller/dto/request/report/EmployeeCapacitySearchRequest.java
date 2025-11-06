package ru.seller_support.assignment.controller.dto.request.report;

import lombok.Builder;
import lombok.Data;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDate;
import java.util.List;

@Value
@Builder(toBuilder = true)
@Jacksonized
@Data
public class EmployeeCapacitySearchRequest {
    LocalDate from;
    LocalDate to;
    String username;
    List<String> workplaces;
}
