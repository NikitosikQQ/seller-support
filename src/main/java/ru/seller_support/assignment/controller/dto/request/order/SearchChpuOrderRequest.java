package ru.seller_support.assignment.controller.dto.request.order;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import ru.seller_support.assignment.domain.enums.OrderStatus;

import java.util.List;

@Value
@Builder
@Jacksonized
public class SearchChpuOrderRequest {

    List<OrderStatus> statuses;

    Integer thickness;

    Integer colorNumber;

    String materialName;

    List<String> excludeMaterialNames;
}
