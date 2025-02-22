package com.github.dfauth.ta.functional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import static com.github.dfauth.ta.functional.Lists.headAndTail;
import static java.util.Optional.empty;

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

    public static <T> Optional<T> allPresent(Function<T,?> fn, T... ts) {
        return allPresent(fn, Arrays.stream(ts).map(Optional::ofNullable).toList());
    }

    public static <T> Optional<T> allPresent(Function<T,?> fn, Optional<T>... ts) {
        return allPresent(fn, Arrays.stream(ts).toList());
    }
    public static <T> Optional<T> allPresent(Function<T,?> fn, List<Optional<T>> ts) {
        return headAndTail(ts).map((h,t) -> {
            if(t.isEmpty()) {
                return empty();
            } else if(t.size() == 1) {
                return h.flatMap(_h -> t.get(0).map(_t -> ((Function<T,T>)fn.apply(_h)).apply(_t)));
            } else {
                return allPresent(h.map(_h -> (Function<T,?>)fn.apply(_h)).orElseThrow(), t);
            }
        });
    }
}
