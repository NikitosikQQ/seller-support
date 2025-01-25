package ru.seller_support.assignment.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class MaterialUpdateRequest {

    @NotBlank(message = "не должен быть пустым")
    String currentName;

    String updatedName;

    String separatorName;

    String sortingPostingBy;
}
