package com.github.dfauth.ta.functional;

import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.function.*;
import java.util.stream.Collector;

@Slf4j
public class MutableCollector<T,A,R> implements Collector<T,A,R> {

    private final A initial;
    private final BiFunction<A, T, A> accumulator;
    protected final Function<A, R> finisher;

    public MutableCollector(A initial, BiFunction<A, T, A> accumulator, Function<A, R> finisher) {
        this.initial = initial;
        this.accumulator = accumulator;
        this.finisher = finisher;
    }

    public static <T,A,R> MutableCollector<T,A,R> mutableCollector(A initial, BiFunction<A,T,A> accumulator, Function<A,R> finisher) {
        return new MutableCollector<>(initial, accumulator, finisher);
    }

    @Override
    public Supplier<A> supplier() {
        return () -> initial;
    }

    @Override
    public BiConsumer<A, T> accumulator() {
        return accumulator::apply;
    }

    @Override
    public BinaryOperator<A> combiner() {
        return (a1,a2) -> {
            throw new IllegalStateException("Parallel operations not supported");
        };
    }

    @Override
    public Function<A, R> finisher() {
        return finisher;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Set.of();
    }

}
