package ru.seller_support.assignment.util;

import lombok.experimental.UtilityClass;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@UtilityClass
public class CommonUtils {

    public static final int COUNT_OF_SECONDS_FOR_END_OF_DAY = 86399;
    public static final String EMPTY_STRING = "";
    public static final String SPACE = " ";
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy")
            .withZone(ZoneOffset.UTC);
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy H:mm")
            .withZone(ZoneOffset.UTC);
    public static final DateTimeFormatter REQUEST_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static final Duration MOSCOW_OFFSET = Duration.parse("PT3H");


    public static Instant parseStringToInstantOzon(String date, boolean isFrom) {
        if (Objects.isNull(date) || date.isEmpty()) {
            return null;
        }
        LocalDate resultDate = LocalDate.parse(date, REQUEST_DATE_FORMATTER);
        return isFrom
                ? resultDate.atStartOfDay(ZoneOffset.UTC).toInstant()
                : resultDate.atStartOfDay(ZoneOffset.UTC).toInstant().plusSeconds(COUNT_OF_SECONDS_FOR_END_OF_DAY);
    }

    public static Instant toMoscowTime(Instant instant) {
        return instant.plus(MOSCOW_OFFSET);
    }

    public static Instant parseStringToInstant(String date) {
        if (Objects.isNull(date) || date.isEmpty()) {
            return null;
        }
        LocalDate resultDate = LocalDate.parse(date, REQUEST_DATE_FORMATTER);
        return resultDate.atStartOfDay(ZoneOffset.UTC).toInstant().plusSeconds(COUNT_OF_SECONDS_FOR_END_OF_DAY);
    }

    public static String formatInstantToDateTimeString(Instant instant) {
        return formatInstantToString(DATE_TIME_FORMATTER, instant);
    }

    public static String formatInstantToDateString(Instant instant) {
        return formatInstantToString(DATE_FORMATTER, instant);
    }

    public static String formatInstantToString(DateTimeFormatter formatter, Instant instant) {
        return formatter.format(instant);
    }

    public static String getFormattedStringWithInstant(String pattern, Instant instant) {
        String date = CommonUtils.DATE_FORMATTER.format(instant);
        return String.format(pattern, date);
    }
}
