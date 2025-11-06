package ru.seller_support.assignment.controller.dto.response.order;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import ru.seller_support.assignment.domain.enums.Marketplace;
import ru.seller_support.assignment.domain.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@Jacksonized
public class OrderDto {

    private String number;

    private OrderStatus status;

    private Integer palletNumber;

    private Marketplace marketplace;

    private String shopName;

    private LocalDateTime inProcessAt;

    private String article;

    private Integer quantity;

    private Integer length;

    private Integer width;

    private Integer thickness;

    private BigDecimal totalPrice;

    private BigDecimal areaInMeters;

    private BigDecimal pricePerSquareMeter;

    private Integer colorNumber;

    private String color;

    private String comment;

    private String materialName;
}
