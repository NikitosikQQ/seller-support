package ru.seller_support.assignment.controller.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@Jacksonized
public class WorkplaceDto {
    private UUID id;
    private String workplace;
    private BigDecimal rate;
}
