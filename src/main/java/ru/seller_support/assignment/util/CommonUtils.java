package ru.seller_support.assignment.util;

import lombok.experimental.UtilityClass;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@UtilityClass
public class CommonUtils {

    public static final ZoneId UTC_ZONE_ID = ZoneId.of("UTC");
    public static final ZoneId MOSCOW_ZONE_ID = ZoneId.of("Europe/Moscow");

    public static final int COUNT_OF_SECONDS_FOR_END_OF_DAY = 86399;
    public static final String EMPTY_STRING = "";
    public static final String SPACE = " ";
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy")
            .withZone(ZoneOffset.UTC);
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy H:mm")
            .withZone(ZoneOffset.UTC);
    public static final DateTimeFormatter REQUEST_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static Instant parseStringToInstantOzon(String date, boolean isFrom) {
        if (Objects.isNull(date) || date.isEmpty()) {
            return null;
        }
        LocalDate resultDate = LocalDate.parse(date, REQUEST_DATE_FORMATTER);
        return isFrom
                ? resultDate.atStartOfDay(ZoneOffset.UTC).toInstant()
                : resultDate.atStartOfDay(ZoneOffset.UTC).toInstant().plusSeconds(COUNT_OF_SECONDS_FOR_END_OF_DAY);
    }

    public static LocalDateTime toMoscowLocalDateTime(Instant instant) {
        return instant.atZone(MOSCOW_ZONE_ID).toLocalDateTime();
    }

    public static Instant parseStringToInstant(String date) {
        if (Objects.isNull(date) || date.isEmpty()) {
            return null;
        }
        LocalDate resultDate = LocalDate.parse(date, REQUEST_DATE_FORMATTER);
        return resultDate.atStartOfDay(ZoneOffset.UTC).toInstant().plusSeconds(COUNT_OF_SECONDS_FOR_END_OF_DAY);
    }

    public static String formatToDateTimeString(LocalDateTime time) {
        return DATE_TIME_FORMATTER.format(time);
    }

    public static String formatInstantToDateString(Instant instant) {
        return formatInstantToString(DATE_FORMATTER, instant);
    }

    public static String formatLocalDateTimeToDateString(LocalDateTime localDateTime) {
        return DATE_FORMATTER.format(localDateTime);
    }

    public static String formatInstantToString(DateTimeFormatter formatter, Instant instant) {
        return formatter.format(instant);
    }

    public static String getFormattedStringWithInstant(String pattern, Instant instant) {
        String date = CommonUtils.DATE_FORMATTER.format(instant);
        return String.format(pattern, date);
    }
}
