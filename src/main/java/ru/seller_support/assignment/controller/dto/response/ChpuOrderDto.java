package ru.seller_support.assignment.controller.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@Jacksonized
public class ChpuOrderDto {
    private String shortArticle;
    private BigDecimal areaSummary;
    private List<String> orderNumbers;
}
