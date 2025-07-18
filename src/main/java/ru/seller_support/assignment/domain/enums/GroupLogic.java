package ru.seller_support.assignment.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.thymeleaf.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@AllArgsConstructor
public enum GroupLogic {

    AND("И"),
    OR("ИЛИ"),
    XOR("ТОЛЬКО ОДНО ИЗ УСЛОВИЙ"),
    NOTHING("БЕЗ ГРУППИРОВОК");

    private String value;

    public static GroupLogic fromSymbol(String symbol) {
        if (StringUtils.isEmpty(symbol)) {
            return NOTHING;
        }
        for (GroupLogic op : values()) {
            if (op.getValue().equalsIgnoreCase(symbol)) {
                return op;
            }
        }
        throw new IllegalArgumentException("Неизвестное условие группировки: " + symbol);
    }

    public static List<GroupLogic> getGroupLogicSymbols() {
        return new ArrayList<>(Arrays.asList(values()));
    }
}
