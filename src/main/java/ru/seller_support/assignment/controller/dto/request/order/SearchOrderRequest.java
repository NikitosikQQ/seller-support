package ru.seller_support.assignment.controller.dto.request.order;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import ru.seller_support.assignment.domain.enums.Marketplace;
import ru.seller_support.assignment.domain.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
@Jacksonized
public class SearchOrderRequest {

    @NotNull
    Integer page;
    @NotNull
    Integer size;

    String number;

    List<OrderStatus> statuses;

    List<Marketplace> marketplaces;

    String shopName;

    Integer length;

    Integer width;

    Integer thickness;

    Integer colorNumber;

    String materialName;
    List<String> materialNames;

    LocalDateTime toInProcessAt;
    LocalDateTime fromInProcessAt;

    List<String> excludeMaterialNames;

    String sortingType;
}
