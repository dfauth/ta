package com.github.dfauth.ta.functional;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

public interface Consecutive<T,R> {

    Optional<R> swap(T t);

    static <T,R> Consecutive<T,R> swappable(BiFunction<T,T,R> f2) {

        AtomicReference<T> prev = new AtomicReference<>(null);
        return t -> {
            try {
                return Optional.ofNullable(prev.get())
                        .map(_t -> f2.apply(_t,t));
            } finally {
                prev.set(t);
            }
        };
    }
}
