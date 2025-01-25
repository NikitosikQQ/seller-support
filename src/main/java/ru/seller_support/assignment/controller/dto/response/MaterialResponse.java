package ru.seller_support.assignment.controller.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class MaterialResponse {
    private String name;
    private String separatorName;
    private String sortingPostingBy;
}
