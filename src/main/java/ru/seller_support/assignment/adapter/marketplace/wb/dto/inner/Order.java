package ru.seller_support.assignment.adapter.marketplace.wb.dto.inner;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;

@Value
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Order {

    //номер отправления
    @JsonProperty(required = true)
    Long id;

    //артикул товара
    @JsonProperty(required = true)
    String article;

    //цена за товар в заказе
    @JsonProperty(required = true)
    Long convertedPrice;

    //дата и время принятия в обработку
    @JsonProperty(required = true)
    Instant createdAt;
}
