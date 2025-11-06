package ru.seller_support.assignment.controller.dto.request.color;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.UUID;

@Value
@Builder
@Jacksonized
public class ColorDeleteRequest {
    @NotNull
    UUID id;
}
