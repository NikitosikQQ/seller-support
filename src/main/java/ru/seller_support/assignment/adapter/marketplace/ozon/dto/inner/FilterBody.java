package ru.seller_support.assignment.adapter.marketplace.ozon.dto.inner;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;

@Data
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FilterBody {

    //начала периода времени, до которого продавцу нужно собрать заказ
    @JsonProperty(required = true, value = "cutoff_from")
    Instant cutoffFrom;

    //конец периода времени, до которого продавцу нужно собрать заказ
    @JsonProperty(required = true, value = "cutoff_to")
    Instant cutoffTo;

    //статус отправления
    @JsonProperty(value = "status")
    String status;
}
