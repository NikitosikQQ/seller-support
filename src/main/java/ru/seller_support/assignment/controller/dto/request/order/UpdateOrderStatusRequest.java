package ru.seller_support.assignment.controller.dto.request.order;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import ru.seller_support.assignment.domain.enums.OrderStatus;

@Value
@Builder
@Jacksonized
public class UpdateOrderStatusRequest {
    String number;
    OrderStatus newStatus;
}
