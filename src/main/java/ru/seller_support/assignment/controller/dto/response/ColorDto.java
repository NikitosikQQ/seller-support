package ru.seller_support.assignment.controller.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.UUID;

@Data
@Builder
@Jacksonized
public class ColorDto {
    private UUID id;
    private Integer number;
    private String name;
}
