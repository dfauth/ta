package com.github.dfauth.ta.util;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.valueOf;

public class Accumulator<T,R> implements Function<T,R>, Supplier<R> {

    private final Function<T, R> f;
    private R result;

    public Accumulator(R initial, BiFunction<R, Integer, R> divisor, BiFunction<R, T, R> accumulator) {
        f = avg(initial, divisor, accumulator);
    }

    public static Accumulator<BigDecimal,BigDecimal> averagingAccumulator() {
        return new Accumulator<>(ZERO, BigDecimalOps::divide, BigDecimal::add);
    }

    public static Accumulator<Integer,BigDecimal> intAveragingAccumulator() {
        return new Accumulator<>(ZERO, BigDecimalOps::divide, (bd,i) -> bd.add(valueOf(i)));
    }

    public static Function<BigDecimal,BigDecimal> avg() {
        return avg(ZERO, BigDecimalOps::divide, BigDecimal::add);
    }

    public static <T,R> Function<T,R> avg(R r, BiFunction<R,Integer,R> divisor, BiFunction<R,T,R> accumulator) {
        AtomicInteger i = new AtomicInteger(0);
        AtomicReference<R> ref = new AtomicReference<>(r);
        return t -> {
            i.incrementAndGet();
            ref.set(accumulator.apply(ref.get(), t));
            return divisor.apply(ref.get(), i.get());
        };
    }

    public R apply(T t) {
        result = f.apply(t);
        return result;
    }

    @Override
    public R get() {
        return result;
    }
}
