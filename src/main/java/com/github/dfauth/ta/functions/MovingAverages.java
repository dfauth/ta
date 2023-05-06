package com.github.dfauth.ta.functions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Function;

import static com.github.dfauth.ta.functions.Accumulator.BD_ACCUMULATOR;

public class MovingAverages {

    public static Function<BigDecimal, Optional<BigDecimal>> sma(int period) {
        return sma(period, BD_ACCUMULATOR.get());
    }

    public static <T extends Number, R extends Number> Function<T, Optional<R>> sma(int period, Accumulator<T,R> ops) {
        LinkedList<T> l = new LinkedList<>();
        return t -> {
            l.add(t);
            ops.add(t);
            if(l.size() > period) {
                ops.subtract(l.remove(0));
                return Optional.of(ops.divide(l.size()));
            } else {
                return Optional.empty();
            }
        };
    }

    public static Function<BigDecimal, Optional<BigDecimal>> ema(int period) {
        if (period <= 0) {
            throw new IllegalArgumentException("Period should be a positive integer");
        }
        BigDecimal alpha = BigDecimal.valueOf(2.0 / (period + 1));
        var ref = new Object() {
            BigDecimal ema = BigDecimal.ZERO;
            int count = 0;
        };
        return t -> {
                if (ref.count < period) {
                    ref.ema = ref.ema.add(t);
                    ref.count++;
                    if (ref.count == period) {
                        ref.ema = ref.ema.divide(BigDecimal.valueOf(period), RoundingMode.HALF_UP);
                        return Optional.of(ref.ema);
                    } else {
                        return Optional.empty();
                    }
                } else {
                    ref.ema = alpha.multiply(t.subtract(ref.ema)).add(ref.ema);
                    return Optional.of(ref.ema);
                }
        };
    }
}
