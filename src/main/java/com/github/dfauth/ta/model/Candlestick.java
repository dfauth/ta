package com.github.dfauth.ta.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface Candlestick extends PriceAction {

    String getCode();
    LocalDate getDate();

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
