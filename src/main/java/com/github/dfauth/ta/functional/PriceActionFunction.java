package com.github.dfauth.ta.functional;

import com.github.dfauth.ta.model.PriceAction;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static java.math.BigDecimal.ZERO;

public abstract class PriceActionFunction<A,R> extends SimpleCollector<PriceAction,A,R> {

    static PriceActionFunction<PriceAction,PriceAction> sma(int period) {
        return generate(
                (pa1,pa2) -> pa1.add(pa2),
                pa -> pa.divide(period)
        );
    }

    static PriceActionFunction<PriceAction,PriceAction> generate(BinaryOperator<PriceAction> f2, UnaryOperator<PriceAction> f1) {
        return generate(ZERO_PRICE_ACTION, f2, f1);
    }

    static <T> PriceActionFunction<List<PriceAction>,T> generateList(Function<List<PriceAction>,T> f) {
        return generate(List.of(), Lists::add, f);
    }

    static <A,R> PriceActionFunction<A,R> generate(A initial, BiFunction<A,PriceAction,A> accumulator, Function<A,R> finisher) {
        return new PriceActionFunction<>() {
            @Override
            public Function<AtomicReference<A>, R> finisher() {
                return (ref) -> finisher.apply(ref.get());
            }

            @Override
            public A initial() {
                return initial;
            }

            @Override
            public BiFunction<A, PriceAction, A> accumulate() {
                return accumulator;
            }
        };
    }
    public static PriceAction ZERO_PRICE_ACTION = new PriceAction(){
        @Override
        public BigDecimal getOpen() {
            return ZERO;
        }

        @Override
        public BigDecimal getHigh() {
            return ZERO;
        }

        @Override
        public BigDecimal getLow() {
            return ZERO;
        }

        @Override
        public BigDecimal getClose() {
            return ZERO;
        }

        @Override
        public int getVolume() {
            return 0;
        }
    };
}
