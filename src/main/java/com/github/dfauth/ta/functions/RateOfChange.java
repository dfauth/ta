package com.github.dfauth.ta.functions;

import com.github.dfauth.ta.functional.WindowReducer;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static com.github.dfauth.ta.util.ArrayRingBuffer.windowfy;
import static java.math.RoundingMode.HALF_UP;

public class RateOfChange {

    public static Function<BigDecimal, Optional<BigDecimal>> roc() {
        final AtomicReference<BigDecimal> previous = new AtomicReference<>(null);
        return t -> {
            try {
                return Optional.ofNullable(previous.get())
                        .map(p -> t.divide(p, 3, HALF_UP).subtract(BigDecimal.ONE));
            } finally {
                previous.set(t);
            }
        };
    }

    public static Function<BigDecimal, Optional<BigDecimal>> roc(int period) {
        return windowfy(period, WindowReducer.windowReducer(period, c -> c.stream().reduce(BigDecimal::subtract)));
    }

    public static Function<BigDecimal, Optional<BigDecimal>> sma(int period) {
        Function<BigDecimal, BigDecimal> divideByPeriod = _tot -> _tot.divide(BigDecimal.valueOf(period), HALF_UP);
        return windowfy(period, WindowReducer.windowReducer(period, c -> c.stream().reduce(BigDecimal::add).map(divideByPeriod)));
    }
}
