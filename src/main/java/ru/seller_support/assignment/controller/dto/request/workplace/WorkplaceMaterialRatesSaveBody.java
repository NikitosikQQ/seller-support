package ru.seller_support.assignment.controller.dto.request.workplace;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;

@Value
@Builder
@Jacksonized
public class WorkplaceMaterialRatesSaveBody {

    @NotBlank
    String workplace;

    @NotBlank
    String materialName;

    @NotNull
    @DecimalMin(value = "0.0", message = "Ставка не может быть отрицательной")
    BigDecimal coefficient;

    @DecimalMin(value = "0.0", message = "Ставка не может быть отрицательной")
    BigDecimal minAreaInMeters;

    @DecimalMin(value = "0.0", message = "Ставка не может быть отрицательной")
    BigDecimal maxAreaInMeters;
}
