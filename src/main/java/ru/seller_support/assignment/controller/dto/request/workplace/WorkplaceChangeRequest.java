package ru.seller_support.assignment.controller.dto.request.workplace;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;
import java.util.UUID;

@Value
@Builder
@Jacksonized
public class WorkplaceChangeRequest {
    @NotNull
    UUID id;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true, message = "Ставка не может быть отрицательной")
    BigDecimal rate;
}
