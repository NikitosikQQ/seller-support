package ru.seller_support.assignment.controller.dto.request.article;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class ArticleSaveRequest {

    @NotBlank(message = "не должен быть пустым")
    String name;

    String type;

    String materialName;

    @Min(1)
    Integer quantityPerSku;

    String chpuMaterialName;
}
