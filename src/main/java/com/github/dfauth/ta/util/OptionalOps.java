package com.github.dfauth.ta.util;

import java.util.Optional;
import java.util.function.BiFunction;

public class OptionalOps {

    public static <T> Iterable<T> toIterable(Optional<T> o) {
        return () -> o.stream().iterator();
    }

    public static <T,R> BiFunction<Optional<T>,Optional<T>,Optional<R>> apply(BiFunction<T,T,R> f) {
        return (o1,o2) -> o1.flatMap(_o1 -> o2.map(_o2 -> f.apply(_o1, _o2)));
    }
}
