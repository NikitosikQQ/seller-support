package ru.seller_support.assignment.controller.dto.response;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class MaterialCoefficientResponseDto {
    UUID id;
    String materialName;
    BigDecimal coefficient;
    BigDecimal minArea;
    BigDecimal maxArea;
}
