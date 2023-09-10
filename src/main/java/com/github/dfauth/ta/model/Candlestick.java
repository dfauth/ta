package com.github.dfauth.ta.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface Candlestick {

    String getCode();
    LocalDate getDate();
    BigDecimal getOpen();
    BigDecimal getHigh();
    BigDecimal getLow();
    BigDecimal getClose();
    int getVolume();
    default BigDecimal getTrueRange(BigDecimal previousClose) {
        return getRange()
                .max(getHigh().subtract(previousClose).abs()
                        .max(getLow().subtract(previousClose).abs()
                        )
                );
    }

    default BigDecimal getRange() {
        return getHigh().subtract(getLow());
    }

    default boolean isRising() {
        return getClose().compareTo(getOpen()) > 0;
    }

    default boolean isFalling() {
        return getClose().compareTo(getOpen()) < 0;
    }

    default boolean isBefore(Candlestick candlestick) {
        return getDate().isBefore(candlestick.getDate());
    }

    default boolean isAfter(Candlestick candlestick) {
        return getDate().isAfter(candlestick.getDate());
    }

    default boolean isEngulfing(Candlestick previous) {
        return (isRising() && gapDown(previous) && closedHigher(previous)) ||
                (isFalling() && gapUp(previous) && closedLower(previous)) ;
    }

    default boolean gapDown(Candlestick previous) {
        return previous.isBefore(this) && getOpen().compareTo(previous.getClose()) < 0;
    }

    default boolean gapUp(Candlestick previous) {
        return previous.isBefore(this) && getOpen().compareTo(previous.getClose()) > 0;
    }

    default boolean closedHigher(Candlestick previous) {
        return previous.isBefore(this) && getClose().compareTo(previous.getClose()) > 0;
    }

    default boolean closedLower(Candlestick previous) {
        return previous.isBefore(this) && getClose().compareTo(previous.getClose()) < 0;
    }
}
