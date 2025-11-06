package ru.seller_support.assignment.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;
import java.util.Set;

@Getter
@AllArgsConstructor
public enum OrderStatus {

    CREATED, PILA, CHPU, KROMKA, UPAKOVKA, BRAK;

    public static final Set<OrderStatus> FINAL_STATUSES = Set.of(UPAKOVKA);

    public static final Set<OrderStatus> STATUSES_FOR_DOWNLOAD_PACKAGES = Set.of(PILA, CHPU, KROMKA);

    public static final Map<Workplace, OrderStatus> SUCCESS_WORKPLACE_ORDER_STATUS_MAP = Map.of(
            Workplace.PILA_MASTER, PILA,
            Workplace.PILA1, PILA,
            Workplace.PILA2, PILA,
            Workplace.CHPU, CHPU,
            Workplace.KROMSHIK, KROMKA,
            Workplace.UPAKOVSHIK, UPAKOVKA,
            Workplace.UPAKOVSHIK_MEBEL, UPAKOVKA
    );

    public static final Map<Workplace, Set<OrderStatus>> BRAK_WORKPLACE_ORDER_STATUS_MAP = Map.of(
            Workplace.PILA_MASTER, Set.of(CREATED, PILA),
            Workplace.PILA1, Set.of(CREATED, PILA),
            Workplace.PILA2, Set.of(CREATED, PILA),
            Workplace.CHPU, Set.of(CREATED, CHPU),
            Workplace.KROMSHIK, Set.of(CHPU, PILA, KROMKA),
            Workplace.UPAKOVSHIK, Set.of(CHPU, PILA, KROMKA, UPAKOVKA),
            Workplace.UPAKOVSHIK_MEBEL, Set.of(CREATED, UPAKOVKA)
    );

    public boolean canUpdateToNewStatus(OrderStatus newStatus, Workplace workplace) {
        if (newStatus != BRAK) {
            return switch (this) {
                case CREATED -> newStatus == PILA || newStatus == CHPU || (newStatus == UPAKOVKA && workplace == Workplace.UPAKOVSHIK_MEBEL);
                case PILA, CHPU -> newStatus == KROMKA || (newStatus == UPAKOVKA && workplace == Workplace.UPAKOVSHIK);
                case KROMKA -> newStatus == UPAKOVKA && workplace == Workplace.UPAKOVSHIK;
                case BRAK -> newStatus == CREATED;
                case UPAKOVKA -> false;
            };
        }
        var expectedStatuses = BRAK_WORKPLACE_ORDER_STATUS_MAP.get(workplace);
        return expectedStatuses.contains(this);
    }
}
