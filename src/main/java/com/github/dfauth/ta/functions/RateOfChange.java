package com.github.dfauth.ta.functions;

import com.github.dfauth.ta.util.RingBuffer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static java.math.BigDecimal.ZERO;

public class RateOfChange {

    public static Function<BigDecimal, Optional<BigDecimal>> roc() {
        final AtomicReference<BigDecimal> previous = new AtomicReference<>(null);
        return t -> {
            try {
                return Optional.ofNullable(previous.get())
                        .map(p -> t.divide(p, 3, RoundingMode.HALF_UP).subtract(BigDecimal.ONE));
            } finally {
                previous.set(t);
            }
        };
    }

    public static Function<BigDecimal, Optional<BigDecimal>> roc(int len) {
        RingBuffer<BigDecimal> window = new RingBuffer<>(() -> new BigDecimal[len]);
        AtomicReference<BigDecimal> sum = new AtomicReference<>(ZERO);
        return t -> {
            sum.set(sum.get().add(t));
            window.add(t).ifPresent(v -> sum.set(sum.get().subtract(v)));
            return Optional.of(sum.get()).filter(x -> window.isFull()).map(v -> v.divide(BigDecimal.valueOf(len), RoundingMode.HALF_UP));
        };
    }
}
