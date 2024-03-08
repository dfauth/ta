package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.functional.Collectors;
import io.github.dfauth.trycatch.ExceptionalRunnable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;

public interface ControllerMixIn {
    static <T> Function<Throwable, T> logAndReturn(T t) {
        return ex -> {
            ExceptionalRunnable.log.accept(ex);
            return t;
        };
    }

    default  <T> Map<String,T> mapCode(List<List<String>> codes, Function<String,T> f) {
        return codes.stream()
                .flatMap(List::stream)
                .filter(not(String::isEmpty))
                .map(code -> Map.entry(code, f.apply(code)))
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> (k,v) -> Optional.ofNullable(v).map(_v -> e.getValue()).orElseGet(e::getValue))
                );
    }

    default  <T> Map<String,T> flatMapCode(List<List<String>> codes, Function<String, Stream<T>> f) {
        return codes.stream()
                .flatMap(List::stream)
                .filter(not(String::isEmpty))
                .flatMap(code -> f.apply(code).map(v -> Map.entry(code, v)))
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> (k,v) -> Optional.ofNullable(v).map(_v -> e.getValue()).orElseGet(e::getValue))
                );
    }
}
