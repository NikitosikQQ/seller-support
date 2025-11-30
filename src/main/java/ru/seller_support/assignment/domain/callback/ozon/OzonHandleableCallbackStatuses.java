package ru.seller_support.assignment.domain.callback.ozon;

import lombok.experimental.UtilityClass;
import ru.seller_support.assignment.domain.enums.OrderStatus;

import java.util.Map;
import java.util.Set;

@UtilityClass
public class OzonHandleableCallbackStatuses {

    public static final String DELIVERING = "posting_on_way_to_city";
    public static final String CANCELLED = "posting_canceled";

    public static final Set<String> HANDLEABLE_STATUSES = Set.of(DELIVERING, CANCELLED);

    public static final Map<String, OrderStatus> OZON_STATUS_ORDER_STATUS_MAP = Map.of(
            DELIVERING, OrderStatus.DONE,
            CANCELLED, OrderStatus.CANCELLED
    );
}
