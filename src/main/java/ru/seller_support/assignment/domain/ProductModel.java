package ru.seller_support.assignment.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;

@Data
@Builder(toBuilder = true)
@Jacksonized
@AllArgsConstructor
@NoArgsConstructor
public class ProductModel {
    private String article;
    private BigDecimal price;
    private Integer quantity;
    private Integer length;
    private Integer width;
    private BigDecimal totalPrice;
    private BigDecimal areaInMeters;
    private BigDecimal pricePerSquareMeter;
    private Integer colorNumber;
    private String color;
    private String promoName;
    private String comment;

    @Builder.Default
    private Boolean wrongArticle = false;

    @Builder.Default
    private Boolean wrongBox = false;
}
