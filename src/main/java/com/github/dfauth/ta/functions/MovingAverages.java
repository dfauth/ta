package com.github.dfauth.ta.functions;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Function;

import static com.github.dfauth.ta.functions.Accumulator.BD_ACCUMULATOR;

public class MovingAverages {

    public static Function<BigDecimal, Optional<BigDecimal>> sma(int period) {
        return sma(period, BD_ACCUMULATOR);
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
}
