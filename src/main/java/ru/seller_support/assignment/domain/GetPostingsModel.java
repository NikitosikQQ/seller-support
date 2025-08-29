package ru.seller_support.assignment.domain;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import ru.seller_support.assignment.controller.dto.request.WbSupplyDetails;

import java.time.Instant;
import java.util.List;

@Data
@Builder(toBuilder = true)
@Jacksonized
public class GetPostingsModel {
    private Instant from;
    private Instant to;
    private Instant yandexTo;
    private List<WbSupplyDetails> wbSupplies;
    private Instant excludeFromOzon;
    private Instant excludeToOzon;
    private String ozonStatus;
}
