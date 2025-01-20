package ru.seller_support.assignment.controller.dto.response;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import ru.seller_support.assignment.service.enums.Marketplace;

import java.util.UUID;

@Value
@Builder
@Jacksonized
public class ShopResponse {

    UUID id;
    String name;
    Marketplace marketplace;
    Integer palletNumber;
}
