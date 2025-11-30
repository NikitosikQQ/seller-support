package ru.seller_support.assignment.controller.dto.request.order;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class VerifyOrderRequest {

    // проставляется на стороне скрипта упаковщиков, если фасад был собран, а не просто пикнута этикетка
    Boolean isEmployeePreparedFacade;

    String orderNumber;
}
