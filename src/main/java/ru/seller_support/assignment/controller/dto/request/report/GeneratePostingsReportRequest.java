package ru.seller_support.assignment.controller.dto.request.report;

import lombok.Builder;
import lombok.Data;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import ru.seller_support.assignment.controller.dto.request.WbSupplyDetails;

import java.time.Instant;
import java.util.List;

@Value
@Builder(toBuilder = true)
@Jacksonized
@Data
public class GeneratePostingsReportRequest {
    String from;
    String to;
    String yandexTo;
    List<WbSupplyDetails> supplies;
    Instant excludeFromOzon;
    Instant excludeToOzon;

}
