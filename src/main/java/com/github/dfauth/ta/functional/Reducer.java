package com.github.dfauth.ta.functional;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;
import java.util.stream.Collector;

import static com.github.dfauth.ta.functional.FunctionUtils.unsupported;

public interface Reducer<T,R> {

    R initial();

    default BiFunction<R,T,R> accumulator() {
        return (r,t) -> {
            accumulatingConsumer().accept(r,t);
            return r;
        };
    }

    default BiConsumer<R,T> accumulatingConsumer() {
        return (r,t) -> {};
    }

    default BinaryOperator<R> combiner() {
        return unsupported();
    }

    static <T,R> ReducerCollector<T, AtomicReference<R>,R> with(Reducer<T,R> reducer) {
        return new ReducerCollector<>(new Reducer<>() {
            @Override
            public AtomicReference<R> initial() {
                return new AtomicReference<>(reducer.initial());
            }

            @Override
            public BiConsumer<AtomicReference<R>, T> accumulatingConsumer() {
                BiFunction<R, T, R> f = reducer.accumulator();
                return (ar, t) -> ar.set(f.apply(ar.get(), t));
            }

            @Override
            public BinaryOperator<AtomicReference<R>> combiner() {
                return (ar1, ar2) -> new AtomicReference<>(reducer.combiner().apply(ar1.get(), ar2.get()));
            }
        }, AtomicReference::get);
    }

    class ReducerCollector<T,S,R> implements Collector<T,S,R> {

        private final Reducer<T, S> reducer;
        private Function<S,R> finisher;

        public ReducerCollector(Reducer<T,S> reducer, Function<S,R> finisher) {
            this.reducer = reducer;
            this.finisher = finisher;
        }

        public Supplier<S> supplier() {
            return reducer::initial;
        }

        public BiConsumer<S, T> accumulator() {
            return reducer.accumulatingConsumer();
        }

        public BinaryOperator<S> combiner() {
            return unsupported();
        }

        public Function<S, R> finisher() {
            return finisher;
        }

        public Set<Characteristics> characteristics() {
            return Set.of(Characteristics.UNORDERED);
        }
    }

}
