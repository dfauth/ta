package com.github.dfauth.ta.functional;

import com.github.dfauth.ta.model.Price;
import com.github.dfauth.ta.model.PriceAction;
import com.github.dfauth.ta.util.ArrayRingBuffer;
import com.github.dfauth.ta.util.RingBuffer;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.github.dfauth.ta.functional.StatefulFunction.asFunction;
import static com.github.dfauth.ta.functional.StatefulFunction.toStatefulFunction;
import static com.github.dfauth.ta.model.PriceAction.divisionOperator;
import static java.math.BigDecimal.ONE;
import static java.math.RoundingMode.HALF_UP;

public class StatefulFunctions {

    static StatefulFunction<BigDecimal, BigDecimal, BigDecimal> _roc() {
        return (t,s) -> new Tuple2<>(Optional.ofNullable(s).map(t::subtract),t);
    }

    static BiFunction<BigDecimal, RingBuffer<BigDecimal>, Optional<BigDecimal>> _sma(int period) {
        Function<BigDecimal,BigDecimal> divideByPeriod = bd -> bd.divide(BigDecimal.valueOf(period), HALF_UP);
        return (t,state) -> {
            RingBuffer<BigDecimal> newState = Optional.ofNullable(state).orElse(ArrayRingBuffer.create(period));
            newState.write(t);
            Optional<BigDecimal> r = Optional.ofNullable(state)
                    .filter(RingBuffer::isFull)
                    .flatMap(_s -> _s.stream().reduce(BigDecimal::add).map(divideByPeriod));
            return r;
        };
    }

    static BiFunction<Price, RingBuffer<PriceAction>, Optional<PriceAction>> _smaCloseVol(int period) {
        return (t,state) -> {
            RingBuffer<PriceAction> newState = Optional.ofNullable(state).orElse(new ArrayRingBuffer<>(new PriceAction[period]));
            newState.write(t);
            Optional<PriceAction> r = Optional.ofNullable(state)
                    .filter(RingBuffer::isFull)
                    .flatMap(_s -> _s.stream().reduce((pa1,pa2) -> pa1.add(pa2)).map(pa -> pa.map(divisionOperator(period))));
            return r;
        };
    }

    public static Function<BigDecimal, Optional<BigDecimal>> roc() {
        return asFunction(_roc());
    }

    public static Function<BigDecimal, Optional<BigDecimal>> sma(int period) {
        return asFunction(toStatefulFunction(_sma(period)));
    }

    public static Function<Price, Optional<PriceAction>> smaCloseVol(int period) {
        return asFunction(toStatefulFunction(_smaCloseVol(period)));
    }

    public static Function<BigDecimal, Optional<BigDecimal>> periodWkLo(int period) {
        return asFunction(toStatefulFunction(_periodWkLo(period)));
    }

    static BiFunction<BigDecimal, RingBuffer<BigDecimal>, Optional<BigDecimal>> _periodWkLo(int period) {
        return (t, rb) -> {
            rb.write(t);
            return rb.stream().reduce(BigDecimal::min).map(_min -> t.divide(_min, HALF_UP).subtract(ONE));
        };
    }

}
