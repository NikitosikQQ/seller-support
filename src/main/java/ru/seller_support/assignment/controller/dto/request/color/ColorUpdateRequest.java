package ru.seller_support.assignment.controller.dto.request.color;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.UUID;

@Value
@Builder
@Jacksonized
public class ColorUpdateRequest {

    @NotNull
    UUID id;

    Integer number;

    String name;
}
