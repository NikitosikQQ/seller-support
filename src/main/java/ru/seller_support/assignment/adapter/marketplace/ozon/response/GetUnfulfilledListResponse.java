package ru.seller_support.assignment.adapter.marketplace.ozon.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import ru.seller_support.assignment.adapter.marketplace.ozon.inner.Posting;

import java.util.List;

@Value
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetUnfulfilledListResponse {

    @JsonProperty(value = "result", required = true)
    Result result;

    @Data
    @Builder(toBuilder = true)
    @Jacksonized
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {

        @JsonProperty(value = "postings", required = true)
        List<Posting> postings;
    }

}
