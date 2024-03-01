package com.github.dfauth.ta.util;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class OptionalOps {

    public static <T> Iterable<T> toIterable(Optional<T> o) {
        return () -> o.stream().iterator();
    }

    public static <T,R> Function<Optional<T>,Optional<R>> apply(Function<T,R> f) {
        return o -> o.map(f);
    }

    public static <T,R,S> BiFunction<Optional<T>,Optional<R>,Optional<S>> apply(BiFunction<T,R,S> f) {
        return (o1,o2) -> o1.flatMap(_o1 -> o2.map(_o2 -> f.apply(_o1, _o2)));
    }

    public static <T,R,S> BiFunction<Optional<T>,Optional<R>,Optional<S>> applyAndFlatten(BiFunction<T,R,Optional<S>> f) {
        return (o1,o2) -> o1.flatMap(_o1 -> o2.flatMap(_o2 -> f.apply(_o1, _o2)));
    }
}
