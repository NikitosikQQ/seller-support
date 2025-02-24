package ru.seller_support.assignment.adapter.marketplace.yandexmarket.dto.inner;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;

@Data
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Item {
    private String offerId;
    private BigDecimal price;
    private Integer count;
}
