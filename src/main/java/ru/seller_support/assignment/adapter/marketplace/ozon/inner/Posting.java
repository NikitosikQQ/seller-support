package ru.seller_support.assignment.adapter.marketplace.ozon.inner;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;
import java.util.List;

@Value
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Posting {

    //номер отправления
    @JsonProperty(required = true, value = "posting_number")
    String postingNumber;

    //дата принятия в обработку
    @JsonProperty(required = true, value = "in_process_at")
    Instant inProcessAt;

    //информация по товарам в отправлении
    @JsonProperty(required = true, value = "products")
    List<Product> products;
}
