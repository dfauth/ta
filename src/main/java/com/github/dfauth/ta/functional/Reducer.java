package com.github.dfauth.ta.functional;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;
import java.util.stream.Collector;

import static com.github.dfauth.ta.functional.FunctionUtils.unsupported;

public interface Reducer<T,S,R> extends Collector<T,S,R> {

    @Override
    default Supplier<S> supplier() {
        return this::initial;
    }

    S initial();

    @Override
    Function<S, R> finisher();

    @Override
    default Set<Characteristics> characteristics() {
        return Set.of(Characteristics.UNORDERED);
    }

    BiConsumer<S,T> accumulator();

    default BiFunction<S,T,S> accumulatingFunction() {
        return (s,t) -> {
            accumulator().accept(s,t);
            return s;
        };
    }

    default BinaryOperator<S> combiner() {
        return unsupported();
    }

    static <T,R> Reducer<T, AtomicReference<R>,R> simple(SimpleReducer<T,R> reducer) {
        return new Reducer<>() {
            @Override
            public AtomicReference<R> initial() {
                return new AtomicReference<>(reducer.initial());
            }

            @Override
            public Function<AtomicReference<R>, R> finisher() {
                return AtomicReference::get;
            }

            @Override
            public BiConsumer<AtomicReference<R>, T> accumulator() {
                return (ar, t) -> ar.set(reducer.accumulatingFunction().apply(ar.get(), t));
            }
        };
    }

}
