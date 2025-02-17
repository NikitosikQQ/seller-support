package ru.seller_support.assignment.adapter.marketplace.wb.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class GetStickersRequest {

    //список id отправлений
    @JsonProperty(required = true)
    List<Long> orders;
}
