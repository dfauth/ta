package com.github.dfauth.ta.util;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.function.BiFunction;

@Slf4j
@AllArgsConstructor
public class ComparableWrapper<T> implements Comparable<ComparableWrapper<T>> {

    private final T nested;
    private final BiFunction<T, T, Integer> comparator;

    @Override
    public int compareTo(ComparableWrapper<T> other) {
        return comparator.apply(nested, other.nested);
    }

    @Override
    public String toString() {
        return nested.toString();
    }

    @Override
    public int hashCode() {
        return nested.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return nested.equals(obj);
    }
}
