package ru.seller_support.assignment.controller.dto.request.order;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import ru.seller_support.assignment.domain.enums.OrderStatus;

import java.time.LocalDateTime;

@Data
@Builder
@Jacksonized
public class OrderHistoryDto {
    private OrderStatus status;
    private String author;
    private String workplace;
    private LocalDateTime createdAt;
}
