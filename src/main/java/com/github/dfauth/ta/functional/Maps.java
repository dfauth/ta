package com.github.dfauth.ta.functional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.*;
import java.util.stream.Collectors;

import static com.github.dfauth.ta.functional.Collectors.*;
import static com.github.dfauth.ta.functions.Reducers.groupBy;
import static com.github.dfauth.ta.functions.Reducers.latest;
import static java.util.function.Function.identity;

public class Maps<K,V> extends HashMap<K,V> {

    public Maps(Map<K,V> map) {
        super(map);
    }

    public static <K,V> Map<K,V> merge(Map<K,V>... maps) {
        return merge(oops("map merge not supported"), maps);
    }

    public static <K,V> Map<K,V> merge(BinaryOperator<V> mergeFunction, Map<K,V>... maps) {
        return merge(HashMap::new, mergeFunction, maps);
    }

    public static <K,V, T extends Map<K,V>> T merge(Supplier<T> supplier, BinaryOperator<V> mergeFunction, Map<K,V>... maps) {
        return Arrays.stream(maps).flatMap(m -> m.entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, mergeFunction, supplier));
    }

    public static <K,V,T> Map<K,T> mapValues(Map<K,V> l, Function<V,T> f) {
        return new Maps<>(l).mapValues(f);
    }

    public static <K,V,T,R> Map<T,R> map(Map<K,V> l, Function<K,T> keyMapper, Function<V,R> valueMapper) {
        return new Maps<>(l).map(keyMapper, valueMapper);
    }

    public static <K,V,T,R> Map<T,R> mapEntries(Map<K,V> l, BiFunction<K,V,Map.Entry<T,R>> f) {
        return new Maps<>(l).mapEntries(f);
    }

    public static <K,V> Maps<K,V> of() {
        return new Maps<>(Map.of());
    }

    public static <K,V> Maps<K,V> of(K k, V v) {
        return new Maps<>(Map.of(k,v));
    }

    public static <K,V> Maps<K,V> of(K k, V v, K k1, V v1) {
        return new Maps<>(Map.of(k,v,k1,v1));
    }

    public static <K,V> Maps<K,V> of(Map<K,V> m) {
        return new Maps<>(m);
    }

    public static <K,V> Maps<K,V> maps(Map<K,V> m) {
        return of(m);
    }

    public <R> Maps<K,R> mapValues(Function<V,R> valueMapper) {
        return map(identity(), valueMapper);
    }

    public <T,R> Maps<T,R> mapEntries(BiFunction<K,V,Map.Entry<T,R>> f2) {
        return entrySet()
                .stream()
                .map(_e -> f2.apply(_e.getKey(), _e.getValue()))
                .collect(groupBy(
                        Maps.of(),
                        identity(),
                        identity(),
                        latest())
                );
    }

    public <T,R> Maps<T,R> map(Function<K,T> keyMapper, Function<V,R> valueMapper) {
        return entrySet()
                .stream()
                .collect(groupBy(
                        Maps.of(),
                        keyMapper,
                        valueMapper,
                        latest())
                );
    }

    public Maps<K,V> filterValue(Predicate<V> predicate) {
        return maps(entrySet().stream().filter(e -> predicate.test(e.getValue())).collect(mapEntryMap()));
    }
}
