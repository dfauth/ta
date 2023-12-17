package com.github.dfauth.ta.functional;

import java.util.Set;
import java.util.function.*;
import java.util.stream.Collector;

public interface MutableCollector<T, A, R> extends Collector<T, A, R> {

    static <T, A, R> MutableCollector<T, A, R> of(A initial, BiConsumer<A, T> accumulator, Function<A, R> finisher) {
        return new MutableCollector<>() {
            @Override
            public BiConsumer<A, T> accumulator() {
                return accumulator;
            }

            @Override
            public Function<A, R> finisher() {
                return finisher;
            }

            @Override
            public A initial() {
                return initial;
            }

        };
    }

    @Override
    default Supplier<A> supplier() {
        return this::initial;
    }

    A initial();

    @Override
    default BinaryOperator<A> combiner() {
        return (a1, a2) -> {
            throw new IllegalArgumentException("Parallel operations not supported");
        };
    }

    @Override
    default Set<Characteristics> characteristics() {
        return Set.of();
    }
}
