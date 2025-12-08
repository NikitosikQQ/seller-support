package ru.seller_support.assignment.controller.dto.request.workplace;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.UUID;

@Value
@Builder
@Jacksonized
public class WorkplaceMaterialRatesDeleteBody {

    @NotNull
    UUID id;
}
