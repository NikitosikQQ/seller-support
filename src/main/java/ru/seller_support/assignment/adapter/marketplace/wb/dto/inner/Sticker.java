package ru.seller_support.assignment.adapter.marketplace.wb.dto.inner;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Sticker {

    //id заказа
    @JsonProperty(required = true)
    String orderId;

    //первая часть ID стикера
    @JsonProperty(required = true)
    String partA;

    //вторая часть ID стикера
    @JsonProperty(required = true)
    String partB;

    //закодированные байты этикетки в base64(формат svg)
    @JsonProperty(required = true)
    String file;
}
