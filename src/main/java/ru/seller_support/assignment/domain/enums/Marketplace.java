package ru.seller_support.assignment.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Marketplace {
    OZON("ОЗОН"),
    WILDBERRIES("WB"),
    YANDEX_MARKET("ЯНДЕКС");

    private final String value;
}
