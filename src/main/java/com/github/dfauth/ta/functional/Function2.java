package com.github.dfauth.ta.functional;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Function2 {

    public static <T> Function<T,T> peek(Consumer<T> c) {
        return t -> {
            c.accept(t);
            return t;
        };
    }

    public static <T,R> Function<T,R> supply(Supplier<R> supplier) {
        return t -> supplier.get();
    }

    public static <T,R,S> Function<T,Function<R,S>> curry(BiFunction<T,R,S> f2) {
        return t -> r -> f2.apply(t,r);
    }
    public static <T,R,S> Function<T,Function<R,S>> leftCurry(BiFunction<T,R,S> f2) {
        return curry(f2);
    }
    public static <T,R,S> Function<R,Function<T,S>> rightCurry(BiFunction<T,R,S> f2) {
        return r -> t -> f2.apply(t,r);
    }
}
