package com.github.dfauth.ta.functional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static com.github.dfauth.ta.functional.Collectors.mapEntryMap;
import static com.github.dfauth.ta.functional.Collectors.oops;
import static com.github.dfauth.ta.functions.Reducers.groupBy;
import static com.github.dfauth.ta.functions.Reducers.latest;
import static java.util.Arrays.stream;
import static java.util.function.Function.identity;

public class Maps<K,V> extends HashMap<K,V> {

    public Maps(Map<K,V> map) {
        super(map);
    }

    public static <K,V> Map<K,V> merge(Map<K,V>... maps) {
        return merge(oops("map merge not supported"), maps);
    }

    public static <K,V> BinaryOperator<Map<K,V>> merge(BinaryOperator<V> mergeFunction) {
        return (l, r) -> merge(HashMap::new, mergeFunction, l, r);
    }

    public static <K,V,T extends Map<K,V>> Map<K,V> merge(BinaryOperator<V> mergeFunction, T... maps) {
        return merge(HashMap::new, mergeFunction, maps);
    }

    public static <K,V, T extends Map<K,V>> T merge(Supplier<T> supplier, BinaryOperator<V> mergeFunction, Map<K,V>... maps) {
        return stream(maps).flatMap(m -> m.entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, mergeFunction, supplier));
    }

    public static <K,V,T> Map<K,T> mapValues(Map<K,V> l, Function<V,T> f) {
        return new Maps<>(l).mapValues(f);
    }

    public static <K,V,T> Map<K,T> mapValues(Map<K,V> l, BiFunction<K,V,T> f) {
        return new Maps<>(l).mapValues(f);
    }

    public static <K,V,T,R> Map<T,R> map(Map<K,V> l, Function<K,T> keyMapper, Function<V,R> valueMapper) {
        return new Maps<>(l).map(keyMapper, valueMapper);
    }

    public static <K,V,M extends Map<K,V>> Collector<Entry<K, V>, ?, M> mapEntries(Supplier<M> supplier, BinaryOperator<V> f) {
        return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, f, supplier);
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

    public static <K,T, M extends Map<K,List<T>>> M from(Supplier<M> initial, Function<T,K> keyMapper, T... ts) {
        return from(initial, keyMapper, List::of, listMerge(), ts);
    }

    public static <T,K,V,M extends Map<K,V>> M from(Supplier<M> initial, Function<T,K> keyMapper, Function<T,V> valueMapper, BinaryOperator<V> mergeFn, T... ts) {
        return stream(ts).reduce(initial.get(),
                (m,t) -> {
                    m.computeIfPresent(keyMapper.apply(t), (k,v) -> mergeFn.apply(v, valueMapper.apply(t)));
                    m.computeIfAbsent(keyMapper.apply(t), k -> valueMapper.apply(t));
                    return m;
                },
                (l,r) -> merge(initial, mergeFn, l, r)
        );
    }

    public static <K,V> Maps<K,V> maps(Map<K,V> m) {
        return of(m);
    }

    public static <T> BinaryOperator<List<T>> listMerge() {
        return Lists::add;
    }

    public static <K,T> Function<T, Map.Entry<K,List<T>>> mapEntry(Function<T,K> f) {
        return t -> Map.entry(f.apply(t), List.of(t));
    }

    public <R> Maps<K,R> mapValues(Function<V,R> valueMapper) {
        return map(identity(), valueMapper);
    }

    public <R> Map<K,R> mapValues(BiFunction<K,V,R> valueMapper) {
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

    public <T,R> Map<T,R> map(Function<K,T> keyMapper, BiFunction<K,V,R> valueMapper) {
        return entrySet().stream().map(e -> Map.entry(keyMapper.apply(e.getKey()), valueMapper.apply(e.getKey(), e.getValue()))).collect(mapEntryMap());
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
