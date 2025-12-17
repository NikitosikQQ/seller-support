package ru.seller_support.assignment.domain.callback.ozon;

import lombok.experimental.UtilityClass;
import ru.seller_support.assignment.domain.enums.OrderStatus;

import java.util.Map;
import java.util.Set;

@UtilityClass
public class OzonHandleableCallbackStatuses {

    public static final String DELIVERING1 = "posting_on_way_to_city";
    public static final String DELIVERING2 = "posting_transferred_to_courier_service";
    public static final String DELIVERING3 = "posting_in_courier_service";
    public static final String DELIVERING4 = "posting_on_way_to_pickup_point";
    public static final String DELIVERING5 = "posting_in_pickup_point";
    public static final String DELIVERING6 = "posting_conditionally_delivered";
    public static final String DELIVERING7 = "posting_driver_pick_up";
    public static final String DELIVERED1 = "posting_delivered";
    public static final String DELIVERED2 = "posting_received";
    public static final String CANCELLED = "posting_canceled";

    public static final Set<String> HANDLEABLE_STATUSES = Set.of(
            DELIVERING1,
            DELIVERING2,
            DELIVERING3,
            DELIVERING4,
            DELIVERING5,
            DELIVERING6,
            DELIVERING7,
            DELIVERED1,
            DELIVERED2,
            CANCELLED);

    public static final Map<String, OrderStatus> OZON_STATUS_ORDER_STATUS_MAP = Map.of(
            DELIVERING1, OrderStatus.DONE,
            DELIVERING2, OrderStatus.DONE,
            DELIVERING3, OrderStatus.DONE,
            DELIVERING4, OrderStatus.DONE,
            DELIVERING5, OrderStatus.DONE,
            DELIVERING6, OrderStatus.DONE,
            DELIVERING7, OrderStatus.DONE,
            DELIVERED1, OrderStatus.DONE,
            DELIVERED2, OrderStatus.DONE,
            CANCELLED, OrderStatus.CANCELLED
    );
}
