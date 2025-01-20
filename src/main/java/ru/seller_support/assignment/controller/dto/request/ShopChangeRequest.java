package ru.seller_support.assignment.controller.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import ru.seller_support.assignment.domain.enums.Marketplace;

import java.util.UUID;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class ShopChangeRequest {

    @NotNull(message = "не должно быть пустым")
    UUID id;

    @Size(max = 32, message = "не должно превышать 32 символа")
    String name;

    @Min(1)
    Integer palletNumber;

    String apiKey;

    Marketplace marketplace;

    String clientId;
}
