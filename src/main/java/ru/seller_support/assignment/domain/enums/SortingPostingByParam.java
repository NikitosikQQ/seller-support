package ru.seller_support.assignment.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum SortingPostingByParam {
    COLOR_NUMBER("Номер цвета"),
    PROMO_NAME("Номер акции/цены"),
    COLOR_NAME("Наименование цвета");

    private final String value;

    public static SortingPostingByParam of(String value) {
        return Arrays.stream(values())
                .filter(param -> param.getValue().equalsIgnoreCase(value))
                .findFirst()
                .orElse(COLOR_NUMBER);
    }

}
