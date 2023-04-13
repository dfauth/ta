package com.github.dfauth.ta.functions;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static com.github.dfauth.ta.functions.Accumulator.BD_ACCUMULATOR;
import static java.lang.reflect.Array.newInstance;

public class RateOfChange {

    public static Function<BigDecimal, Optional<BigDecimal>> roc() {
        return roc(1);
    }

    public static Function<BigDecimal, Optional<BigDecimal>> roc(int period) {
        return roc(period, BD_ACCUMULATOR);
    }

    public static <T extends Number, R extends Number> Function<T, Optional<R>> roc(int period, Accumulator<T,R> ops) {
        LinkedList<T> l = new LinkedList<>();
        return t -> {
            l.add(t);
            if(l.size() < period+1) {
                return Optional.empty();
            } else {
                if(l.size() > period+1) {
                    l.remove(0);
                }
                ops.set(l.get(period));
                ops.subtract(l.get(0));
                return Optional.of(ops.divide(period));
            }
        };
    }
}
