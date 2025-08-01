package ru.seller_support.assignment.controller.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class ArticleResponse {
    private String name;
    private String type;
    private Integer quantityPerSku;
    private String materialName;
    private String chpuMaterialName;
}
