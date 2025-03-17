package com.github.dfauth.ta.functional;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;
import java.util.stream.Collector;

import static com.github.dfauth.ta.functional.Collectors.oops;
import static com.github.dfauth.ta.functional.Tuple2.tuple2;

public class Consecutive<T,R> implements Collector<T,Consecutive<T,R>,R> {

    private final AtomicReference<Tuple2<R,T>> ref;
    private final Function<R, BiFunction<T, T, R>> f2;

    static <T,R> Consecutive<T,R> consecutive(R r, Function<R, BiFunction<T,T,R>> f2) {
        return new Consecutive<>(r, f2);
    }

    static <T,R> Consecutive<T,R> consecutive(R r, BiConsumer<T,T> consumer) {
        return new Consecutive<>(r, consumer);
    }

    public Consecutive(R r, BiConsumer<T,T> consumer) {
        this(r, ignored -> (left,right) -> {
            consumer.accept(left,right);
            return r;
        });
    }

    public Consecutive(R r, Function<R, BiFunction<T,T,R>> f2) {
        ref = new AtomicReference<>(tuple2(r, null));
        this.f2 = f2;
    }

    @Override
    public Supplier<Consecutive<T, R>> supplier() {
        return () -> this;
    }

    @Override
    public BiConsumer<Consecutive<T, R>, T> accumulator() {
        return (c, t) -> {
            var t2 = c.ref.get();
            var acc = t2._1();
            var prev = t2._2();
            var r1 = Optional.ofNullable(prev)
                    .map(_t -> c.f2.apply(acc).apply(_t, t))
                    .orElse(acc);
            ref.set(tuple2(r1, t));
        };
    }

    @Override
    public BinaryOperator<Consecutive<T, R>> combiner() {
        return oops();
    }

    @Override
    public Function<Consecutive<T, R>, R> finisher() {
        return c -> c.ref.get()._1();
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Set.of();
    }
}
