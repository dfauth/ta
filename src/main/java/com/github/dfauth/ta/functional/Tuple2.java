package com.github.dfauth.ta.functional;

import java.util.Map;
import java.util.function.Function;

public class Tuple2<K,V> {

    private final K k;
    private final V v;

    public static <K,V> Tuple2<K,V> tuple2(K k, V v) {
        return new Tuple2<>(k,v);
    }

    public Tuple2(K k, V v) {
        this.k = k;
        this.v = v;
    }

    public K _1() {
        return k;
    }

    public V _2() {
        return v;
    }

    public <T> Tuple2<K,T> mapValue(Function<V,T> f) {
        return new Tuple2<>(k, f.apply(v));
    }

    public <T> T map(Function<Tuple2<K,V>,T> f) {
        return f.apply(this);
    }

    public Map.Entry<K,V> toMapEntry() {
        return Map.entry(k,v);
    }
}
