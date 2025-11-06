package ru.seller_support.assignment.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;
import java.util.Set;

@Getter
@AllArgsConstructor
public enum Workplace {
    PILA1("Пила-1"),
    PILA2("Пила-2"),
    PILA_MASTER("Пила-мастер"),
    CHPU("ЧПУ"),
    KROMSHIK("Кромщик"),
    UPAKOVSHIK("Упаковщик"),
    UPAKOVSHIK_MEBEL("Упаковщик мебели");

    public static final Set<Workplace> PILA_WORKPLACES = Set.of(PILA1, PILA2, PILA_MASTER);
    public static final Set<Workplace> UPAKOVSHIK_WORKPLACES = Set.of(UPAKOVSHIK, UPAKOVSHIK_MEBEL);

    private final String value;

    public static Workplace fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (Workplace workplace : Workplace.values()) {
            if (workplace.getValue().equalsIgnoreCase(value)) {
                return workplace;
            }
        }
        throw new IllegalArgumentException("Неизвестное значение рабочего места: " + value);
    }

    public static Set<Workplace> getWorkplacesGroupByWorkplace(Workplace workplace) {
        if (workplace == null) {
            return Collections.emptySet();
        }
        return switch (workplace) {
            case PILA1, PILA2, PILA_MASTER -> PILA_WORKPLACES;
            case CHPU -> Set.of(CHPU);
            case KROMSHIK -> Set.of(KROMSHIK);
            case UPAKOVSHIK, UPAKOVSHIK_MEBEL -> UPAKOVSHIK_WORKPLACES;
        };
    }
}
