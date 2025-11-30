package ru.seller_support.assignment.adapter.marketplace.wb.dto.request;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class GetOrderStatusRequest {
    private List<Long> orders;
}
