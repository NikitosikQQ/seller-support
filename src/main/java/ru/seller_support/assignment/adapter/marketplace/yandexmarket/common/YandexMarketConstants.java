package ru.seller_support.assignment.adapter.marketplace.yandexmarket.common;

import lombok.experimental.UtilityClass;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static ru.seller_support.assignment.util.CommonUtils.MOSCOW_ZONE_ID;

@UtilityClass
public class YandexMarketConstants {

    public static final DateTimeFormatter GET_SHIPMENT_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy").withZone(ZoneOffset.UTC);

    public static final DateTimeFormatter CREATION_DATE_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").withZone(MOSCOW_ZONE_ID);

    public static class ShipmentStatus {
        public static final String OUTBOUND_READY_FOR_CONFIRMATION = "OUTBOUND_READY_FOR_CONFIRMATION";
        public static final String OUTBOUND_CREATED = "OUTBOUND_READY_FOR_CONFIRMATION";
    }
}
