package ru.seller_support.assignment.adapter.marketplace.wb.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;

@Value
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SupplyInfoResponse {

    @JsonProperty(required = true)
    String id;

    @JsonProperty(required = true)
    Instant createdAt;
}
