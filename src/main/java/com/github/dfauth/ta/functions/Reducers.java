package com.github.dfauth.ta.functions;

import com.github.dfauth.ta.functional.Reducer;
import com.github.dfauth.ta.functional.SimpleReducer;
import com.github.dfauth.ta.functional.Tuple2;
import com.github.dfauth.ta.util.Accumulator;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static com.github.dfauth.ta.functional.FunctionUtils.noOp;
import static com.github.dfauth.ta.util.Accumulator.averagingAccumulator;
import static com.github.dfauth.ta.util.ExceptionalRunnable.tryCatch;
import static java.util.function.Function.identity;
import static java.util.function.Predicate.not;

public class Reducers {

    public static <T> SimpleReducer<T, List<T>> list() {
        return new SimpleReducer<>() {
            @Override
            public List<T> initial() {
                return new ArrayList<>();
            }

            @Override
            public BiConsumer<List<T>, T> accumulator() {
                return List::add;
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


    static <T,K,V> SimpleReducer<T, Map<K,V>> group(Function<T,K> keyMapper, Function<T,V> valueMapper, BinaryOperator<V> valueReducer) {
        return new SimpleReducer<>() {
            @Override
            public Map<K, V> initial() {
                return new HashMap<>();
            }

            @Override
            public BiConsumer<Map<K, V>, T> accumulator() {
                return (m, t) -> {
                    m.compute(keyMapper.apply(t), (k,v) -> Optional.ofNullable(v).map(_v -> valueReducer.apply(_v,valueMapper.apply(t))).orElse(valueMapper.apply(t)));
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


    public static <T> BinaryOperator<T> latest() {
        return (t1,t2) -> t2;
    }

    public static <T> Reducer<CompletableFuture<T>, Tuple2<CompletableFuture<List<T>>,List<CompletableFuture<T>>>,CompletableFuture<List<T>>> future() {
        CompletableFuture<List<T>> fut = new CompletableFuture<>();
        List<CompletableFuture<T>> list = new ArrayList<>();
        return new Reducer<>(){
            @Override
            public Tuple2<CompletableFuture<List<T>>, List<CompletableFuture<T>>> initial() {
                return new Tuple2<>(fut, list);
            }

            @Override
            public BiConsumer<Tuple2<CompletableFuture<List<T>>, List<CompletableFuture<T>>>, CompletableFuture<T>> accumulator() {
                return (t2, f) -> {
                    t2._2().add(f);
                    f.thenAccept(t -> t2._2()
                            .stream()
                            .filter(not(CompletableFuture::isDone))
                            .findFirst()
                            .ifPresentOrElse(
                                    noOp(),
                                    () -> t2._1().complete(t2._2().stream().map(_f -> tryCatch(_f::get)).collect(Collectors.toList()))
                            ));
                };
            }

            @Override
            public Function<Tuple2<CompletableFuture<List<T>>, List<CompletableFuture<T>>>, CompletableFuture<List<T>>> finisher() {
                return Tuple2::_1;
            }
        };
    }

    public static <K,V> Reducer<Map.Entry<K,V>, Map<K,V>,Map<K,V>> groupBy() {
        return groupBy(new HashMap<>(), identity(), identity(), latest());
    }

    public static <K,V,T,R,S extends Map<T,R>> SimpleReducer<Map.Entry<K,V>,S> groupBy(S initial, Function<K,T> keyMapper, Function<V,R> valueMapper, BinaryOperator<R> valueReducer) {
        return new SimpleReducer<>() {
            @Override
            public S initial() {
                return initial;
            }

            @Override
            public BiConsumer<S, Map.Entry<K, V>> accumulator() {
                return (m, e) -> m.compute(
                        keyMapper.apply(e.getKey()),
                        (k, v) -> Optional.ofNullable(v).map(_v -> valueReducer.apply(_v, valueMapper.apply(e.getValue()))).orElseGet(() -> valueMapper.apply(e.getValue()))
                );
            }
        };
    }

    public static Collector<BigDecimal, Accumulator<BigDecimal,BigDecimal>, BigDecimal> sma() {
        return sma(averagingAccumulator());
    }

    public static <T,R> Reducer<T, Accumulator<T,R>, R> sma(Accumulator<T,R> accumulator) {
        return new Reducer<>() {
            @Override
            public Accumulator<T,R> initial() {
                return accumulator;
            }

            @Override
            public Function<Accumulator<T,R>, R> finisher() {
                return Accumulator::get;
            }

            @Override
            public BiConsumer<Accumulator<T,R>, T> accumulator() {
                return Accumulator::apply;
            }
        };
    }
}
