package com.github.dfauth.ta.model;

import java.util.function.Function;

public interface CandlestickComparator<T> {
    T compare(Candlestick previous, Candlestick next);

    default Function<Candlestick,T> curry(Candlestick previous) {
        return next -> compare(previous, next);
    }

    CandlestickComparator<Boolean> ENGULFING_COMPARATOR = (p,n) -> (n.isRising() && n.gapDown(p) && n.closedHigher(p)) ||
            (n.isFalling() && n.gapUp(p) && n.closedLower(p));
    CandlestickComparator<Boolean> GAP_UP = (p,n) -> n.getOpen().compareTo(p.getClose()) > 0;

    CandlestickComparator<Boolean> GAP_DOWN = (p,n) -> n.getOpen().compareTo(p.getClose()) < 0;

    CandlestickComparator<Boolean> CLOSED_HIGHER = (p,n) -> n.getClose().compareTo(p.getClose()) > 0;

    CandlestickComparator<Boolean> CLOSED_LOWER = (p,n) -> n.getClose().compareTo(p.getClose()) < 0;

    CandlestickComparator<Boolean> REDUCED_VOLUME = (p,n) -> n.getVolume() < p.getVolume();

}
