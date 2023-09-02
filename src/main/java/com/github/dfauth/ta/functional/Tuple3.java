package com.github.dfauth.ta.functional;

import java.util.function.Function;

public class Tuple3<K,V,U> extends Tuple {

    public static <K,V,U> Tuple3<K,V,U> tuple3(K k, V v, U u) {
        return new Tuple3<>(k,v,u);
    }

    public Tuple3(K k, V v, U u) {
        super(k,v,u);
    }

    @SuppressWarnings("unchecked")
    public K _1() {
        return (K) valueArray[0];
    }

    @SuppressWarnings("unchecked")
    public V _2() {
        return (V) valueArray[1];
    }

    @SuppressWarnings("unchecked")
    public U _3() {
        return (U) valueArray[2];
    }

    public <T> T map(Function<K,Function<V,Function<U,T>>> f) {
        return f.apply(_1()).apply(_2()).apply(_3());
    }
}
