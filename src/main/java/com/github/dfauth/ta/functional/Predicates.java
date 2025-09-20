package com.github.dfauth.ta.functional;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.function.Predicate.not;

@Slf4j
public class Predicates {

    public static <T> Predicate<T> alwaysTrue() {
        return t -> true;
    }

    public static <T extends Enum<T>> Predicate<T> fromString(String t) {
        return fromString(t, Optional::of);
    }

    public static <T extends Enum<T>, R> Predicate<R> fromString(String t, Function<R, Optional<T>> f) {
        return isNegated(t, r -> f.apply(r).map(Enum::name).map(t::equals).orElse(true));
    }

    public static <T extends Enum<T>> Predicate<T> nameMatches(String name) {
        return t -> name.contains(t.name());
    }

    public static <T> Predicate<T> isNegated(String name, Predicate<T> downstream) {
        return Optional.of(name).filter("!"::startsWith).map(i -> not(downstream)).orElse(downstream);
    }
}
