package com.github.dfauth.ta.functional;

import java.util.function.BiFunction;
import java.util.function.Function;

public class Tuple2<K,V> extends Tuple {

    public static <K,V> Tuple2<K,V> tuple2(K k, V v) {
        return new Tuple2<>(k,v);
    }

    public Tuple2(K k, V v) {
        super(k,v);
    }

    @SuppressWarnings("unchecked")
    public K _1() {
        return (K) valueArray[0];
    }

    @SuppressWarnings("unchecked")
    public V _2() {
        return (V) valueArray[1];
    }

    public <T> T map(BiFunction<K,V,T> f) {
        return map(k -> v -> f.apply(k,v));
    }

    public <T> T map(Function<K,Function<V,T>> f) {
        return f.apply(_1()).apply(_2());
    }

    public <T> Tuple2<K,T> mapValue(Function<V,T> f) {
        return new Tuple2<>(_1(), f.apply(_2()));
    }
}
