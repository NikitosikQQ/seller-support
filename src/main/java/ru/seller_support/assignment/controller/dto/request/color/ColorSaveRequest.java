package ru.seller_support.assignment.controller.dto.request.color;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class ColorSaveRequest {

    @NotNull
    Integer number;

    @NotBlank
    String name;
}
