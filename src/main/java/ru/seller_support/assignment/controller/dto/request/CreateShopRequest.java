package ru.seller_support.assignment.controller.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import ru.seller_support.assignment.domain.enums.Marketplace;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class CreateShopRequest {

    @NotBlank(message = "не должно быть пустым")
    @Size(max = 32, message = "не должно превышать 32 символа")
    String name;

    @NotNull
    @Min(1)
    Integer palletNumber;

    @NotBlank(message = "не должно быть пустым")
    String apiKey;

    @NotNull(message = "не должно быть пустым")
    Marketplace marketplace;

    String clientId;
}
