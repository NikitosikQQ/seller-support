package ru.seller_support.assignment.domain.callback.yandexmarket;

import lombok.experimental.UtilityClass;
import ru.seller_support.assignment.domain.enums.OrderStatus;

import java.util.Map;
import java.util.Set;

@UtilityClass
public class YandexMarketHandleableCallbackStatuses {

    public static final String DELIVERY = "DELIVERY";
    public static final String PICKUP = "PICKUP";
    public static final String DELIVERED = "DELIVERED";
    public static final String PARTIALLY_RETURNED = "PARTIALLY_RETURNED";
    public static final String RETURNED = "RETURNED";
    public static final String CANCELLED = "CANCELLED";

    public static final Set<String> HANDLEABLE_STATUSES = Set.of(
            DELIVERY,
            PICKUP,
            DELIVERED,
            PARTIALLY_RETURNED,
            RETURNED,
            CANCELLED);

    public static final Map<String, OrderStatus> YANDEX_MARKET_STATUS_ORDER_STATUS_MAP = Map.of(
            DELIVERY, OrderStatus.DONE,
            PICKUP, OrderStatus.DONE,
            DELIVERED, OrderStatus.DONE,
            PARTIALLY_RETURNED, OrderStatus.DONE,
            RETURNED, OrderStatus.DONE,
            CANCELLED, OrderStatus.CANCELLED
    );
}
