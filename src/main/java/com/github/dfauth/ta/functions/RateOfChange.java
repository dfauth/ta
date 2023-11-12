package com.github.dfauth.ta.functions;

import com.github.dfauth.ta.functional.WindowFunction;
import com.github.dfauth.ta.functional.WindowReducer;
import com.github.dfauth.ta.util.ArrayRingBuffer;
import com.github.dfauth.ta.util.RingBuffer;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        return windowfy(new BigDecimal[period], WindowReducer.windowReducer(new BigDecimal[period], c -> c.stream().reduce(BigDecimal::subtract)));
    }

    public static Function<BigDecimal, Optional<BigDecimal>> sma(int period) {
        Function<BigDecimal, BigDecimal> divideByPeriod = _tot -> _tot.divide(BigDecimal.valueOf(period), HALF_UP);
        return windowfy(new BigDecimal[period], WindowReducer.windowReducer(new BigDecimal[period], c -> c.stream().reduce(BigDecimal::add).map(divideByPeriod)));
    }

    public static <T,R> Function<T,Optional<R>> windowfy(T[] buffer, WindowReducer<T,R> f) {
        return map(new ArrayRingBuffer<T>(buffer), f);
    }

    public static <T,R> Function<T, Optional<R>> map(RingBuffer<T> buffer, WindowFunction<T, R> f) {
        return t -> {
            buffer.write(t);
            return f.apply(buffer.stream().collect(Collectors.toList()));
        };
    }


}
