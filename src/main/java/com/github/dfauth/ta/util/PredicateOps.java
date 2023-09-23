package com.github.dfauth.ta.util;

import java.util.function.Predicate;
import java.util.stream.Stream;

public class PredicateOps {

    public static <T> Predicate<T> and(Predicate<T>... predicates) {
        return t -> Stream.of(predicates).map(p -> p.test(t)).reduce((b1,b2) -> b1 && b2).orElse(false);
    }

}
