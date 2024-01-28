package com.github.dfauth.ta.util;

import com.github.dfauth.ta.model.Market;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.List;

import static java.time.DayOfWeek.*;
import static java.time.temporal.ChronoField.*;
import static java.time.temporal.ChronoUnit.DAYS;

public class DateOps {

    private static final DateTimeFormatter DTF = new DateTimeFormatterBuilder()
            .appendValue(YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
            .appendValue(MONTH_OF_YEAR, 2)
            .appendValue(DAY_OF_MONTH, 2)
            .toFormatter();
    public static List<DayOfWeek> WEEKDAYS = Arrays.asList(MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY);

    public static Timestamp timestamp(LocalDate localDate, int hour, int minute, ZoneId zoneId) {
        return timestamp(localDate, LocalTime.of(hour,minute), zoneId);
    }

    public static Timestamp timestamp(LocalDate localDate, LocalTime time, ZoneId zoneId) {
        return new Timestamp(localDate.atTime(time).atZone(zoneId).toInstant().toEpochMilli());
    }

    public static Timestamp getCloseMostRecentWeekday(Market market) {
        return timestamp(mostRecentWeekday(market.getOpenDays()), market.getMarketClose(), market.getZone());
    }

    public static LocalDate mostRecentWeekday(List<DayOfWeek> marketOpenDays) {
        return mostRecentWeekday(marketOpenDays, LocalDate.now().minus(1, DAYS));
    }

    public static LocalDate mostRecentWeekday(List<DayOfWeek> marketOpenDays, LocalDate localDate) {
        DayOfWeek dow = localDate.getDayOfWeek();
        return marketOpenDays.contains(dow) ? localDate : mostRecentWeekday(marketOpenDays, localDate.minus(1, DAYS));
    }

    public static String formatDate(TemporalAccessor temporalAccessor) {
        return DTF.format(temporalAccessor);
    }

}
