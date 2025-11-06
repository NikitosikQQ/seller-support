package ru.seller_support.assignment.controller.dto.request.employee;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import ru.seller_support.assignment.domain.enums.CapacityOperationType;

import java.util.List;

@Value
@Builder
@Jacksonized
public class ProcessEmployeeWorkRequest {

    @NotEmpty(message = "не должен быть пустым")
    List<EmployeeDto> employees;

    @NotBlank(message = "Не должен быть пустым")
    String orderNumber;

    @NotNull(message = "Не должен быть пустым")
    CapacityOperationType operationType;
}
