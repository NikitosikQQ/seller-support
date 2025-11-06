package ru.seller_support.assignment.controller.dto.request.order;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import ru.seller_support.assignment.controller.dto.request.WbSupplyDetails;

import java.time.Instant;
import java.util.List;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class ImportOrdersRequest {
    List<WbSupplyDetails> wbSupplyDetails;
    Instant from;
}
