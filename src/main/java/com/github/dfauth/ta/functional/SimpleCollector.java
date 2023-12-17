package com.github.dfauth.ta.functional;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;
import java.util.stream.Collector;

public abstract class SimpleCollector<T,A,R> implements Collector<T,AtomicReference<A>,R> {

    public static <T,A,R> SimpleCollector<T,A,R> reduce(A initial, BiFunction<A, T, A> accumlator, Function<A,R> finisher) {
        return new SimpleCollector<>() {
            @Override
            public A initial() {
                return initial;
            }

            @Override
            public BiFunction<A, T, A> accumulate() {
                return accumlator;
            }

            @Override
            public Function<AtomicReference<A>, R> finisher() {
                return a -> finisher.apply(a.get());
            }
        };
    }

    public Supplier<AtomicReference<A>> supplier() {
        return () -> new AtomicReference<>(initial());
    }

    public abstract A initial();

    @Override
    public BinaryOperator<AtomicReference<A>> combiner() {
        return (a,b) -> {
            throw new UnsupportedOperationException("Parallel operations not supported");
        };
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Set.of(Characteristics.UNORDERED);
    }

    @Override
    public BiConsumer<AtomicReference<A>, T> accumulator() {
        return (ref, t) -> ref.set(accumulate().apply(ref.get(), t));
    }

    public abstract BiFunction<A, T, A> accumulate();

    public static <T> Collector<T,List<T>,List<T>> listCollector() {
        return listCollector(Function.identity());
    }

    public static <T,R> Collector<T,List<T>,R> listCollector(Function<List<T>,R> finisher) {
        return null;
//        return new Collector<>() {
//            @Override
//            public Function<List<T>, R> finisher() {
//                return finisher;
//            }
//
//            @Override
//            public List<T> initial() {
//                return new ArrayList<>();
//            }
//
//            @Override
//            public BiFunction<List<T>, T, List<T>> accumulate() {
//                return Lists::add;
//            }
//        };
    }
}
