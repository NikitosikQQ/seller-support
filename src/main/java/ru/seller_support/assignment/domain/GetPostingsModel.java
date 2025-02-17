package ru.seller_support.assignment.domain;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;

@Data
@Builder(toBuilder = true)
@Jacksonized
public class GetPostingsModel {
    private Instant from;
    private Instant to;
    private String supplyId;
}
