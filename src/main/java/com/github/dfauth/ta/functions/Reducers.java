package com.github.dfauth.ta.functions;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;
import java.util.stream.Collector;

public interface Reducers<T,U> {

    U initial();
    BiFunction<U,T,U> accumulator();
    default BinaryOperator<U> combiner() {
        return (u1,u2) -> {
            throw new UnsupportedOperationException();
        };
    }

    static <T,R> Collector<T, AtomicReference<R>,R> toCollector(Reducers<T,R> reducer) {
        return new Collector<>() {
            @Override
            public Supplier<AtomicReference<R>> supplier() {
                return () -> new AtomicReference<>(reducer.initial());
            }

            @Override
            public BiConsumer<AtomicReference<R>, T> accumulator() {
                return (r,t) -> r.set(reducer.accumulator().apply(r.get(),t));
            }

            @Override
            public BinaryOperator<AtomicReference<R>> combiner() {
                return (r1,r2) -> new AtomicReference<>(reducer.combiner().apply(r1.get(), r2.get()));
            }

            @Override
            public Function<AtomicReference<R>, R> finisher() {
                return AtomicReference::get;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Set.of(Characteristics.UNORDERED);
            }
        };
    }

    static <T> Reducers<T, List<T>> toList() {
        return new Reducers<>() {
            @Override
            public List<T> initial() {
                return new ArrayList<>();
            }

            @Override
            public BiFunction<List<T>, T, List<T>> accumulator() {
                return (l, t) -> {
                    l.add(t);
                    return l;
                };
            }

            @Override
            public BinaryOperator<List<T>> combiner() {
                return (l1, l2) -> {
                    List<T> tmp = new ArrayList<>(l1);
                    tmp.addAll(l2);
                    return tmp;
                };
            }
        };
    }


    static <T,K,V> Reducers<T, Map<K,V>> group(Function<T,K> keyMapper, Function<T,V> valueMapper, BinaryOperator<V> valueReducer) {
        return new Reducers<>() {
            @Override
            public Map<K, V> initial() {
                return new HashMap<>();
            }

            @Override
            public BiFunction<Map<K, V>, T, Map<K, V>> accumulator() {
                return (m, t) -> {
                    m.compute(keyMapper.apply(t), (k,v) -> Optional.ofNullable(v).map(_v -> valueReducer.apply(_v,valueMapper.apply(t))).orElse(valueMapper.apply(t)));
                    return m;
                };
            }

            @Override
            public BinaryOperator<Map<K, V>> combiner() {
                return (m1,m2) -> {
                    Map<K, V> tmp = new HashMap<>(m1);
                    m2.entrySet().forEach(e ->
                        tmp.compute(e.getKey(), (k,v) -> Optional.ofNullable(v).map(_v -> valueReducer.apply(_v,e.getValue())).orElse(e.getValue()))
                    );
                    return tmp;
                };
            }
        };
    }


    static <T> BinaryOperator<T> latest() {
        return (t1,t2) -> t2;
    }
}
