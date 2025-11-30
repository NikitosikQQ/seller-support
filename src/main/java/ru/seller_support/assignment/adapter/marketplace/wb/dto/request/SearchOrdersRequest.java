package ru.seller_support.assignment.adapter.marketplace.wb.dto.request;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class SearchOrdersRequest {
    Long limit;
    Long next;
    Long dateFrom;
    Long dateTo;
}
