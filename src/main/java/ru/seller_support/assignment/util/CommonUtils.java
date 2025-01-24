package ru.seller_support.assignment.util;

import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class CommonUtils {

    public static final int COUNT_OF_SECONDS_FOR_END_OF_DAY = 86398;
    public static final String EMPTY_STRING = "";
    public static final String SPACE = " ";
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy")
            .withZone(ZoneOffset.UTC);
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy H:mm")
            .withZone(ZoneOffset.UTC);
    public static final DateTimeFormatter REQUEST_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    public static Instant parseStringToInstant(String date) {
        LocalDate resultDate = LocalDate.parse(date, REQUEST_DATE_FORMATTER);
        return resultDate.atStartOfDay(ZoneOffset.UTC).toInstant().plusSeconds(COUNT_OF_SECONDS_FOR_END_OF_DAY);
    }

    public static String formatInstant(Instant instant) {
        return DATE_TIME_FORMATTER.format(instant);
    }

    public static String getFormattedStringWithInstant(String pattern, Instant instant) {
        String date = CommonUtils.DATE_FORMATTER.format(instant);
        return String.format(pattern, date);
    }
}
