package com.github.dfauth.ta.model;

import com.github.dfauth.ta.util.DateOps;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public enum MarketEnum implements Market {

    ASX("ASX","Australia/Sydney",LocalTime.of(10,0), LocalTime.of(16,0)),
    NYSE("NYSE","America/New_York",LocalTime.of(9,30), LocalTime.of(16,0));

    private final String code;
    private final ZoneId zoneId;
    private final LocalTime marketOpen;
    private final LocalTime marketClose;
    private final List<DayOfWeek> marketOpenDays;

    MarketEnum(String code, String zoneId, LocalTime marketOpen, LocalTime marketClose) {
        this(code, zoneId, marketOpen, marketClose, DateOps.WEEKDAYS);
    }

    MarketEnum(String code, String zoneId, LocalTime marketOpen, LocalTime marketClose, List<DayOfWeek> marketOpenDays) {
        this.code = code;
        this.zoneId = ZoneId.of(zoneId);
        this.marketOpen = marketOpen;
        this.marketClose = marketClose;
        this.marketOpenDays = marketOpenDays;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public List<DayOfWeek> getOpenDays() {
        return marketOpenDays;
    }

    @Override
    public LocalTime getMarketOpen() {
        return this.marketOpen;
    }

    @Override
    public LocalTime getMarketClose() {
        return this.marketClose;
    }

    @Override
    public ZoneId getZone() {
        return zoneId;
    }

    @Override
    public Collection<LocalDate> getMarketHolidays() {
        return Collections.emptyList();
    }

}
