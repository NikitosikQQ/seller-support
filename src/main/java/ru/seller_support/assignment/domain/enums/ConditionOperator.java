package ru.seller_support.assignment.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.thymeleaf.util.StringUtils;

import java.util.*;

@Getter
@AllArgsConstructor
public enum ConditionOperator {

    EQ("равно"),
    NE("не равно"),
    GT("больше"),
    LT("меньше"),
    GTE("больше или равно"),
    LTE("меньше или равно");

    private final String value;

    public static final Set<ConditionOperator> OPERATORS_FOR_STRING_AND_BOOLEAN = EnumSet.of(EQ, NE);

    public static ConditionOperator fromSymbol(String symbol) {
        if (StringUtils.isEmpty(symbol)) {
            return null;
        }
        for (ConditionOperator op : values()) {
            if (op.getValue().equalsIgnoreCase(symbol)) {
                return op;
            }
        }
        throw new IllegalArgumentException("Неизвестный оператор сравнения: " + symbol);
    }

    public static List<ConditionOperator> getOperators() {
        return new ArrayList<>(Arrays.asList(values()));
    }

}