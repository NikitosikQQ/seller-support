package ru.seller_support.assignment.adapter.marketplace.yandexmarket.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PrepareStickersRequest {
    private String businessId;
    private List<Long> orderIds;
}
