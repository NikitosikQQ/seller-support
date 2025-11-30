package ru.seller_support.assignment.adapter.marketplace.wb.dto.inner;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderStatusInfo {

    Long id;
    String supplierStatus;
    String wbStatus;

}
