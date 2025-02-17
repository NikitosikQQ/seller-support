package ru.seller_support.assignment.adapter.marketplace.wb.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import ru.seller_support.assignment.adapter.marketplace.wb.dto.inner.Order;

import java.util.List;

@Value
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetOrdersBySupplyIdResponse {

    //список отправлений
    @JsonProperty(required = true)
    List<Order> orders;
}
