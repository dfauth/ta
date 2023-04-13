package com.github.dfauth.ta.functions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

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
}
