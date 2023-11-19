package com.github.dfauth.ta.functional;

import com.github.dfauth.ta.model.PriceAction;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;
import java.util.stream.Collector;

import static java.math.BigDecimal.ZERO;

public interface PriceActionFunction<T,A,R> extends Collector<T,AtomicReference<A>,R> {

    default Supplier<AtomicReference<A>> supplier() {
        return () -> new AtomicReference<>(initial());
    }

    A initial();

    @Override
    default BinaryOperator<AtomicReference<A>> combiner() {
        return (a,b) -> {
            throw new UnsupportedOperationException("Parallel operations not supported");
        };
    }

    @Override
    default Set<Characteristics> characteristics() {
        return Set.of(Characteristics.UNORDERED);
    }

    static PriceActionFunction<PriceAction,PriceAction,PriceAction> sma(int period) {
        return generate(
                (pa1,pa2) -> pa1.add(pa2),
                pa -> pa.divide(period)
        );
    }

    static PriceActionFunction<PriceAction,PriceAction,PriceAction> generate(BinaryOperator<PriceAction> f2, UnaryOperator<PriceAction> f1) {
        return generate(ZERO_PRICE_ACTION, f2, f1);
    }

    static <T> PriceActionFunction<PriceAction,List<PriceAction>,T> generateList(Function<List<PriceAction>,T> f) {
        return generate(List.of(), Lists::add, f);
    }

    static <T,A,R> PriceActionFunction<T,A,R> generate(A initial, BiFunction<A,T,A> f2, Function<A,R> f1) {
        return new PriceActionFunction<>() {
            @Override
            public A initial() {
                return initial;
            }

            @Override
            public BiConsumer<AtomicReference<A>, T> accumulator() {
                return (accumulator,pa2) -> accumulator.getAndUpdate(pa1 -> f2.apply(pa1,pa2));
            }

            @Override
            public Function<AtomicReference<A>, R> finisher() {
                return accumulator -> f1.apply(accumulator.get());
            }
        };
    }
    Function<AtomicReference<A>,R> finisher();

    PriceAction ZERO_PRICE_ACTION = new PriceAction(){
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
