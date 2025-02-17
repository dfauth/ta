package com.github.dfauth.ta.functional;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

public class Optionals {

    public static <T> T eitherOrBoth(T t1, T t2, BinaryOperator<T> f2) {
        return Optional.ofNullable(t1).map(_t1 -> Optional.ofNullable(t2).map(_t2 -> f2.apply(_t1,_t2)).orElse(_t1)).orElseGet(() -> t2);
    }

    public static <T,R,S> Optional<S> bothPresent(Optional<T> t, Optional<R> r, BiFunction<T,R,S> f2) {
        return t.flatMap(_t -> r.map(_r -> f2.apply(_t,_r)));
    }

    public static <T,R,S> Optional<S> bothPresent(T t, R r, BiFunction<T,R,S> f2) {
        return bothPresent(Optional.ofNullable(t), Optional.ofNullable(r), f2);
    }
}
