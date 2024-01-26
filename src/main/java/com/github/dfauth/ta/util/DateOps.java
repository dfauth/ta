package com.github.dfauth.ta.util;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import static java.time.DayOfWeek.*;
import static java.time.temporal.ChronoUnit.DAYS;

public class DateOps {

    public static List<DayOfWeek> WEEKDAYS = Arrays.asList(MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY);

    public static Timestamp timestamp(LocalDate localDate, int hour, int minute, ZoneId zoneId) {
        return new Timestamp(localDate.atTime(hour,minute).atZone(zoneId).toInstant().toEpochMilli());
    }

    public static Timestamp sydneyCloseMostRecentWeekday() {
        return sydneyCloseOn(mostRecentWeekday());
    }

    private static LocalDate mostRecentWeekday() {
        return mostRecentWeekday(LocalDate.now().minus(1, DAYS));
    }

    private static LocalDate mostRecentWeekday(LocalDate localDate) {
        DayOfWeek dow = localDate.getDayOfWeek();
        return WEEKDAYS.contains(dow) ? localDate : mostRecentWeekday(localDate.minus(1, DAYS));
    }

    public static Timestamp sydneyCloseToday() {
        return sydneyCloseOn(LocalDate.now());
    }
    public static Timestamp sydneyCloseOn(LocalDate localDate) {
        return timestamp(localDate, 16,0,  ZoneId.of("Australia/Sydney"));
    }


}
