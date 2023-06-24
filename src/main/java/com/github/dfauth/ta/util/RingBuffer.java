package com.github.dfauth.ta.util;

import com.github.dfauth.ta.functional.WindowReducer;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

public interface RingBuffer<T> {

    Optional<T> add(T t);

    Collection<T> toCollection();

    default <R> Function<T, Optional<R>> map(WindowReducer<T, R> f) {
        return t -> {
            add(t);
            return f.apply(toCollection());
        };
    }
}
