package ru.seller_support.assignment.adapter.marketplace.wb.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import ru.seller_support.assignment.adapter.marketplace.wb.dto.inner.OrderStatusInfo;

import java.util.List;

@Value
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetOrdersStatusResponse {

    List<OrderStatusInfo> orders;
}
