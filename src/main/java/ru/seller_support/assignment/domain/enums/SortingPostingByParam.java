package ru.seller_support.assignment.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SortingPostingByParam {
    COLOR_NUMBER("Номер цвета"),
    PROMO_NAME("Номер акции/цены"),
    COLOR_NAME("Наименование цвета");

    private final String value;

}
