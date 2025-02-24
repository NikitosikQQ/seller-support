package ru.seller_support.assignment.adapter.marketplace.yandexmarket.common;

import lombok.experimental.UtilityClass;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class YandexMarketConstants {

    public static final DateTimeFormatter GET_SHIPMENT_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy").withZone(ZoneOffset.UTC);

    public static final DateTimeFormatter CREATION_DATE_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").withZone(ZoneOffset.UTC);

    public static class ShipmentStatus {
        public static final String OUTBOUND_READY_FOR_CONFIRMATION = "OUTBOUND_READY_FOR_CONFIRMATION";
    }
}
