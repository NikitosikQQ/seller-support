package ru.seller_support.assignment.adapter.marketplace.yandexmarket.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetShipmentsRequest {

    //дата создания отгрузки, начала периода
    @JsonProperty(required = true)
    private String dateFrom;

    //дата создания отгрузки, конец периода
    @JsonProperty(required = true)
    private String dateTo;

    //статус отгрузки
    @JsonProperty(required = true)
    private String status;

    //флаг, отвечающий за отмененные в отгрузках заказы в ответе
    @JsonProperty(required = true)
    private Boolean cancelledOrders;

}
