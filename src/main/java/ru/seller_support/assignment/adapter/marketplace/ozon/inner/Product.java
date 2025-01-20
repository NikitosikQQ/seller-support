package ru.seller_support.assignment.adapter.marketplace.ozon.inner;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Product {

    //Артикул
    @JsonProperty(value = "offer_id", required = true)
    String offerId;

    //цена за 1 ед товара, приходит строкой
    @JsonProperty(value = "price", required = true)
    String price;

    //кол-во товара
    @JsonProperty(value = "quantity", required = true)
    Integer quantity;
}
