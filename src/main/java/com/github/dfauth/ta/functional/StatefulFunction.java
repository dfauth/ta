package com.github.dfauth.ta.functional;

import com.github.dfauth.ta.util.ArrayRingBuffer;
import com.github.dfauth.ta.util.RingBuffer;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.math.RoundingMode.HALF_UP;

public interface StatefulFunction<T,R,S> extends BiFunction<T,S,Tuple2<Optional<R>,S>> {

    static <T,R,S> Function<T,Optional<R>> asFunction(StatefulFunction<T,R,S> f) {
        AtomicReference<S> state = new AtomicReference<>();
        return (t) -> {
            Tuple2<Optional<R>, S> t2 = f.apply(t, state.get());
            state.set(t2._2());
            return t2._1();
        };
    }

    static StatefulFunction<BigDecimal, BigDecimal, BigDecimal> _roc() {
        return (t,s) -> new Tuple2<>(Optional.ofNullable(s).map(t::subtract),t);
    }

    static StatefulFunction<BigDecimal, BigDecimal, RingBuffer<BigDecimal>> _sma(int period) {
        Function<BigDecimal,BigDecimal> divideByPeriod = bd -> bd.divide(BigDecimal.valueOf(period), HALF_UP);
        return (t,state) -> {
            RingBuffer<BigDecimal> newState = Optional.ofNullable(state).orElse(new ArrayRingBuffer<>(period));
            newState.add(t);
            Optional<BigDecimal> r = Optional.ofNullable(state)
                    .filter(RingBuffer::isFull)
                    .flatMap(_s -> _s.toCollection().stream().reduce(BigDecimal::add).map(divideByPeriod));
            return new Tuple2<>(r, newState);
        };
    }

    static Function<BigDecimal, Optional<BigDecimal>> roc() {
        return asFunction(_roc());
    }

    static Function<BigDecimal, Optional<BigDecimal>> sma(int period) {
        return asFunction(_sma(period));
    }
}
