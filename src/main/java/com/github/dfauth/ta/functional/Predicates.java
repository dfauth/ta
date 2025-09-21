package com.github.dfauth.ta.functional;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

@Slf4j
public class Predicates {

    public static <T> Predicate<T> alwaysTrue() {
        return t -> true;
    }

    public static <T extends Enum<T>> Predicate<T> fromString(String t) {
        return fromString(t, Optional::of);
    }

    public static <T extends Enum<T>, R> Predicate<R> fromString(String t, Function<R, Optional<T>> f) {
        return isNegated(t, (t1, r) -> f.apply(r)
                .map(Enum::name)
                .map(t1::equals)
                .orElse(true)
        );
    }

    public static <T extends Enum<T>> Predicate<T> nameMatches(String name) {
        return t -> name.contains(t.name());
    }

    public static <T> Predicate<T> isNegated(String name, BiPredicate<String, T> downstream) {
        return t -> Optional.of(name)
                .filter(n -> n.startsWith("!"))
                .map(n -> !downstream
                        .test(n.substring(1), t))
                .orElseGet(() ->
                        downstream.test(name, t));
    }
}
