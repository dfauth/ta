package com.github.dfauth.ta.model;

import com.github.dfauth.ta.functional.Function2;
import com.github.dfauth.ta.util.BigDecimalOps;

import java.math.BigDecimal;
import java.util.function.*;

import static com.github.dfauth.ta.functional.Function2.adapt;
import static java.math.BigDecimal.valueOf;

public interface CandlestickComparator<T> extends BiFunction<Candlestick,Candlestick,T> {
    T compare(Candlestick previous, Candlestick next);

    default T apply(Candlestick previous, Candlestick next) {
        if(previous.isBefore(next)) {
            return compare(previous, next);
        }
        throw new IllegalArgumentException(previous+" is not a preceding candlestick to "+this);
    }

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

    CandlestickComparator<Boolean> INCREASED_VOLUME = (p,n) -> n.getVolume() > p.getVolume();

    BinaryOperator<BigDecimal> PCT_CHANGE = BigDecimalOps::pctChange;
    BiPredicate<BigDecimal,BigDecimal> SIGNIFICANT_RATIO = BigDecimalOps::isGreaterThanOrEqualTo;

    Predicate<Candlestick> SIGNIFICANT_VOLUME = candlestick -> adapt(Function2.curry(SIGNIFICANT_RATIO).apply(valueOf(1.3))).test(valueOf(candlestick.getVolume()));

}
