package com.github.dfauth.ta.model;

import java.util.function.Predicate;

import static com.github.dfauth.ta.util.BigDecimalOps.*;

public interface CandlestickType extends Predicate<Candlestick> {

    static CandlestickType isRising() {
        return candlestick -> isGreaterThan(candlestick.getClose(), candlestick.getOpen());
    }

    static CandlestickType isFalling() {
        return candlestick -> isLessThan(candlestick.getClose(), candlestick.getOpen());
    }

    static CandlestickType isDojo() {
        return candlestick -> candlestick.getClose().equals(candlestick.getOpen());
    }

    static CandlestickType isBullish() {
        return isBullish(0.30);
    }

    static CandlestickType isBullish(double tolerance) {
        return candlestick -> candlestick.isRising() && isGreaterThan(
                divide(candlestick.getHigh().subtract(candlestick.getOpen()), candlestick.getClose().subtract(candlestick.getOpen())),
                valueOf(tolerance)
                );
    }

    static CandlestickType isBearish() {
        return isBearish(0.30);
    }

    static CandlestickType isBearish(double tolerance) {
        return candlestick -> candlestick.isFalling() && isGreaterThan(
                divide(candlestick.getOpen().subtract(candlestick.getLow()), candlestick.getOpen().subtract(candlestick.getClose())),
                valueOf(tolerance)
                );
    }
}
