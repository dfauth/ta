package com.github.dfauth.ta.functional;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;
import java.util.stream.Collector;

import static java.util.function.Function.identity;

public class ImmutableCollector<T, A, R> implements Collector<T, AtomicReference<A>, R> {

    protected final A initial;
    protected final BiFunction<A, T, A> accumulator;
    protected final Function<A, R> finisher;

    public static <T,A> Collector<T, AtomicReference<A>,A> collector(A initial, BiFunction<A,T,A> accumulator) {
        return collector(initial, accumulator, identity());
    }

    public static <T,A,R> Collector<T, AtomicReference<A>,R> collector(A initial, BiFunction<A,T,A> accumulator, Function<A,R> finisher) {
        return new ImmutableCollector<>(initial, accumulator, finisher);
    }

    public ImmutableCollector(A initial, BiFunction<A, T, A> accumulator, Function<A, R> finisher) {
        this.initial = initial;
        this.accumulator = accumulator;
        this.finisher = finisher;
    }

    @Override
    public Supplier<AtomicReference<A>> supplier() {
        return () -> new AtomicReference<>(initial);
    }

    @Override
    public BiConsumer<AtomicReference<A>, T> accumulator() {
        return (a,t) -> a.set(accumulator.apply(a.get(), t));
    }

    @Override
    public BinaryOperator<AtomicReference<A>> combiner() {
        return (a1,a2) -> {
            throw new IllegalStateException("Parallel operations not supported");
        };
    }

    @Override
    public Function<AtomicReference<A>, R> finisher() {
        return a -> finisher.apply(a.get());
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Set.of();
    }
}
