package ru.seller_support.assignment.adapter.marketplace.wb;

import lombok.experimental.UtilityClass;
import ru.seller_support.assignment.domain.enums.OrderStatus;

import java.util.Map;

@UtilityClass
public class WildberriesOrderHandleableStatuses {
    public static final String SORTED = "sorted";
    public static final String SOLD = "sold";
    public static final String CANCELED = "canceled";
    public static final String CANCELED_BY_CLIENT = "canceled_by_client";
    public static final String DECLINED_BY_CLIENT = "declined_by_client";
    public static final String DEFECT = "defect";
    public static final String READY_FOR_PICKUP = "ready_for_pickup";

    public static final Map<String, OrderStatus> WB_STATUS_ORDER_STATUS_MAP = Map.of(
            CANCELED, OrderStatus.CANCELLED, // отмена сборочного задания
            DECLINED_BY_CLIENT, OrderStatus.CANCELLED, // отмена сборочного задания покупателем во время его сборки
            SORTED, OrderStatus.DONE, // сборочное задание отсортировано
            SOLD, OrderStatus.DONE,  // заказ получен покупателем
            DEFECT, OrderStatus.DONE, // отмена заказа по причине брака
            READY_FOR_PICKUP, OrderStatus.DONE, // сборочное задание прибыло на пункт выдачи заказов (ПВЗ)
            CANCELED_BY_CLIENT, OrderStatus.DONE // отмена заказа покупателем при получении = возврат. то есть уже после всех возможных обработок в цеху, поэтому done
    );

}
