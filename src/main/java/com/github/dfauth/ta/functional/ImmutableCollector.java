package com.github.dfauth.ta.functional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;
import java.util.stream.Collector;

import static java.util.function.Function.identity;

public interface ImmutableCollector<T, A, R> extends Collector<T, AtomicReference<A>, R> {

    static <T> ImmutableCollector<T, List<T>, List<T>> toList() {
        return ImmutableCollector.of(new ArrayList<>(),
                Lists::add,
                identity());
    }

    static <T, A, R> ImmutableCollector<Optional<T>, A, R> before(Collector<T, A, R> collector) {
        return before((a, t) -> t, collector);
    }

    static <T, A, R, S> ImmutableCollector<S, A, R> before(BiFunction<A, S, Optional<T>> f2, Collector<T, A, R> collector) {
        return ImmutableCollector.of(collector.supplier().get(),
                (a, s) -> f2.apply(a, s).map(t -> {
                    collector.accumulator().accept(a, t);
                    return a;
                }).orElse(a),
                collector.finisher());
    }

    static <T, A, R, S> ImmutableCollector<T, A, S> finish(Collector<T, A, R> collector, Function<R, S> f) {
        return ImmutableCollector.of(collector.supplier().get(),
                (a,t) -> {
                    collector.accumulator().accept(a,t);
                    return a;
                },
                collector.finisher().andThen(f));
    }

    static <T, A, R> ImmutableCollector<T, A, R> of(A initial, BiFunction<A, T, A> accumulator, Function<A, R> finisher) {
        return new ImmutableCollector<>() {
            @Override
            public A initial() {
                return initial;
            }

            @Override
            public BiFunction<A, T, A> accumulatingFunction() {
                return accumulator;
            }

            @Override
            public Function<A, R> finishingFunction() {
                return finisher;
            }
        };
    }

    @Override
    default Supplier<AtomicReference<A>> supplier() {
        return () -> new AtomicReference<>(initial());
    }

    A initial();

    @Override
    default BiConsumer<AtomicReference<A>, T> accumulator() {
        return (a, t) -> a.set(accumulatingFunction().apply(a.get(), t));
    }

    BiFunction<A, T, A> accumulatingFunction();

    @Override
    default BinaryOperator<AtomicReference<A>> combiner() {
        return (a1, a2) -> {
            throw new IllegalArgumentException("Parallel operations not supported");
        };
    }

    @Override
    default Function<AtomicReference<A>, R> finisher() {
        return a -> finishingFunction().apply(a.get());
    }

    Function<A, R> finishingFunction();

    @Override
    default Set<Characteristics> characteristics() {
        return Set.of();
    }
}
