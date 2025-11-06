package ru.seller_support.assignment.controller.dto.request.material;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class MaterialSaveRequest {

    @NotBlank(message = "не должен быть пустым")
    String name;

    String separatorName;

    String sortingPostingBy;

    @NotNull(message = "не должен быть пустым")
    Boolean useInChpuTemplate;

    String chpuMaterialName;
    String chpuArticleNumber;

    Boolean isOnlyPackaging;
}
