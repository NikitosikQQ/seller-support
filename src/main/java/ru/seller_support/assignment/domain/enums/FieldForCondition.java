package ru.seller_support.assignment.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.thymeleaf.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@AllArgsConstructor
public enum FieldForCondition {

    ORDER_PRICE("Стоимость заказа"),
    SHOP_NAME("Название магазина"),
    WITHOUT_CONDITIONS("Без условий");

    private String viewValue;

    public static FieldForCondition fromSymbol(String symbol) {
        if(StringUtils.isEmpty(symbol)) {
           return WITHOUT_CONDITIONS;
        }
        for (FieldForCondition op : values()) {
            if (op.viewValue.equalsIgnoreCase(symbol)) {
                return op;
            }
        }
        throw new IllegalArgumentException("Unknown operator: " + symbol);
    }

    public static List<FieldForCondition> getFieldsForCondition() {
        return new ArrayList<>(Arrays.asList(values())).stream()
                .filter(field -> field != WITHOUT_CONDITIONS)
                .toList();
    }

}
