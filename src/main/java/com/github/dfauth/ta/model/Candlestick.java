package com.github.dfauth.ta.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Optional;

import static com.github.dfauth.ta.model.CandlestickComparator.*;

public interface Candlestick extends PriceAction {

    String getCode();
    LocalDate getDate();

    default boolean isOfType(CandlestickType candlestickType) {
        return candlestickType.test(this);
    }

    default boolean isRising() {
        return isOfType(CandlestickType.isRising());
    }

    default boolean isFalling() {
        return isOfType(CandlestickType.isFalling());
    }

    default boolean isDojo() {
        return isOfType(CandlestickType.isDojo());
    }

    default <T> T comparePrevious(Candlestick previous, CandlestickComparator<T> comparator) {
        return comparator.apply(previous, this);
    }

    default <T> T compareNext(Candlestick next, CandlestickComparator<T> comparator) {
        return comparator.apply(this, next);
    }

    default boolean isBefore(Candlestick candlestick) {
        return getDate().isBefore(candlestick.getDate());
    }

    default boolean isAfter(Candlestick candlestick) {
        return getDate().isAfter(candlestick.getDate());
    }

    default boolean isEngulfing(Candlestick previous) {
        return comparePrevious(previous, ENGULFING_COMPARATOR);
    }

    default boolean gapDown(Candlestick previous) {
        return comparePrevious(previous, GAP_DOWN);
    }

    default boolean gapUp(Candlestick previous) {
        return comparePrevious(previous, GAP_UP);
    }

    default boolean closedHigher(Candlestick previous) {
        return comparePrevious(previous, CLOSED_HIGHER);
    }

    default boolean closedLower(Candlestick previous) {
        return comparePrevious(previous, CLOSED_LOWER);
    }

    default Optional<BigDecimal> pctDrawDown(Candlestick subsequent) {
        return Optional.ofNullable(subsequent)
                .filter(s -> s.isAfter(this))
                .filter(s -> s.closedLower(this))
                .map(s -> BigDecimal.ONE.subtract(s.getClose().divide(getClose(), RoundingMode.HALF_UP)));
    }
}
