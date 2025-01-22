package ru.seller_support.assignment.adapter.marketplace.ozon.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetPackagesRequest {

    @JsonProperty(required = true, value = "posting_number")
    private List<String> postingNumber;
}
