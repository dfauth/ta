package com.github.dfauth.ta.model;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public interface Market {
    static LocalDate toLocalDate(Market market, TemporalAccessor temporalAccessor) {
        return temporalAccessor.isSupported(ChronoField.INSTANT_SECONDS) ?
                Instant.from(temporalAccessor).atZone(market.getZone()).toLocalDate() :
                LocalDate.from(temporalAccessor);
    }

    static LocalTime toLocalTime(Market market, TemporalAccessor temporalAccessor) {
        return temporalAccessor.isSupported(ChronoField.INSTANT_SECONDS) ?
                Instant.from(temporalAccessor).atZone(market.getZone()).toLocalTime() :
                market.getMarketOpen();
    }

    String getCode();

    List<DayOfWeek> getOpenDays();

    LocalTime getMarketOpen();

    LocalTime getMarketClose();

    ZoneId getZone();

    default boolean isOpen(TemporalAccessor temporalAccessor) {
        LocalDate localDate = toLocalDate(this, temporalAccessor);
        LocalTime localTime = toLocalTime(this, temporalAccessor);
        LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
        return isOpenOn(localDateTime.getDayOfWeek()) &&
                !isHolidayClose(localDateTime.toLocalDate()) &&
                isAtOrAfterMarketOpen(localDateTime.toLocalTime()) &&
                isBeforeMarketClose(localDateTime.toLocalTime());
    }

    default boolean isBeforeMarketClose(LocalTime localTime) {
        return localTime.isBefore(getMarketClose());
    }

    default boolean isAtOrAfterMarketOpen(LocalTime localTime) {
        return !localTime.isBefore(getMarketOpen());
    }

    default boolean isHolidayClose(LocalDate localDate) {
        return getMarketHolidays().contains(localDate);
    }

    Collection<LocalDate> getMarketHolidays();

    default LocalDate getMarketDate() {
        return getMarketDate(Instant.now());
    }

    default boolean isOpenOn(DayOfWeek dayOfWeek) {
        return getOpenDays().contains(dayOfWeek);
    }

    default LocalDate getMarketDate(TemporalAccessor temporalAccessor) {
        LocalDate localDate = toLocalDate(this, temporalAccessor);
        return isOpen(temporalAccessor) ?
                localDate :
                isOpen(localDate.atTime(getMarketOpen())) ?
                        localDate :
                        getMarketDate(localDate.minusDays(1).atTime(getMarketOpen()));
    }

    default Instant atMarketOpenOn(LocalDate localDate) {
        Supplier<Instant> r = () -> {
            throw new IllegalArgumentException("Market is not open on "+localDate);
        };
        return isOpen(localDate) ? localDate.atTime(getMarketOpen()).atZone(getZone()).toInstant() : r.get();
    }

    default Instant atMarketCloseOn(LocalDate localDate) {
        Supplier<Instant> r = () -> {
            throw new IllegalArgumentException("Market is not open on "+localDate);
        };
        return isOpen(localDate) ? localDate.atTime(getMarketClose()).atZone(getZone()).toInstant() : r.get();
    }

    default String withCode(String v) {
        return withCode(this, v);
    }

    static String withCode(Market marktet, String code) {
        return String.format("%s:%s",marktet.getCode(), code);
    }

    default Market withHolidays(List<LocalDate> holidays) {
        return new Market() {
            @Override
            public String getCode() {
                return Market.this.getCode();
            }

            @Override
            public List<DayOfWeek> getOpenDays() {
                return Market.this.getOpenDays();
            }

            @Override
            public LocalTime getMarketOpen() {
                return Market.this.getMarketOpen();
            }

            @Override
            public LocalTime getMarketClose() {
                return Market.this.getMarketClose();
            }

            @Override
            public ZoneId getZone() {
                return Market.this.getZone();
            }

            @Override
            public Collection<LocalDate> getMarketHolidays() {
                return holidays;
            }
        };
    }

    default ZonedDateTime atCloseOn(String date) {
        LocalDate d = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyyMMdd"));
        return d.atTime(getMarketClose()).atZone(getZone());
    }
}
