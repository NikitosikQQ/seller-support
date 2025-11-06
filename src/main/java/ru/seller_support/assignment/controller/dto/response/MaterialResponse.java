package ru.seller_support.assignment.controller.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@Jacksonized
public class MaterialResponse {
    private UUID id;
    private String name;
    private String separatorName;
    private String sortingPostingBy;
    private Boolean useInChpuTemplate;
    private String chpuMaterialName;
    private String chpuArticleNumber;
    private BigDecimal employeeRateCoefficient;
    private Boolean isOnlyPackaging;
}
