package ru.seller_support.assignment.controller.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class ArticleUpdateRequest {

    @NotBlank(message = "не должен быть пустым")
    String currentName;

    String updatedName;

    String type;

    String materialName;

    @Min(1)
    Integer quantityPerSku;
}
