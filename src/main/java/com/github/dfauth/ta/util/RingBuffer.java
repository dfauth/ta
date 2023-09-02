package com.github.dfauth.ta.util;

import com.github.dfauth.ta.functional.WindowFunction;

import java.util.Collection;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface RingBuffer<T> {

    Optional<T> add(T t);

    Stream<T> stream();

    default <R> Function<T, Optional<R>> map(WindowFunction<T, R> f) {
        return t -> {
            add(t);
            return f.apply(stream().collect(Collectors.toList()));
        };
    }

    boolean isFull();
}
