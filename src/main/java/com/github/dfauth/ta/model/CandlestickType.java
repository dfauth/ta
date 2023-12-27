package com.github.dfauth.ta.model;

import java.util.function.Predicate;

import static com.github.dfauth.ta.util.BigDecimalOps.isGreaterThan;
import static com.github.dfauth.ta.util.BigDecimalOps.isLessThan;

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
}
