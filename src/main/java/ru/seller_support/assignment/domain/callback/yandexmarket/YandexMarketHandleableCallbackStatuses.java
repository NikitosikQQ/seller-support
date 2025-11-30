package ru.seller_support.assignment.domain.callback.yandexmarket;

import lombok.experimental.UtilityClass;
import ru.seller_support.assignment.domain.enums.OrderStatus;

import java.util.Map;
import java.util.Set;

@UtilityClass
public class YandexMarketHandleableCallbackStatuses {

    public static final String DELIVERING = "DELIVERY";
    public static final String CANCELLED = "CANCELLED";

    public static final Set<String> HANDLEABLE_STATUSES = Set.of(DELIVERING, CANCELLED);

    public static final Map<String, OrderStatus> YANDEX_MARKET_STATUS_ORDER_STATUS_MAP = Map.of(
            DELIVERING, OrderStatus.DONE,
            CANCELLED, OrderStatus.CANCELLED
    );
}
