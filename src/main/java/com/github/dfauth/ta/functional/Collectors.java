package com.github.dfauth.ta.functional;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;
import java.util.stream.Collector;

import static com.github.dfauth.ta.functional.Lists.last;
import static com.github.dfauth.ta.functional.Lists.nonEmpty;
import static com.github.dfauth.ta.functional.Tuple2.tuple2;
import static com.github.dfauth.ta.util.BigDecimalOps.ONE3;
import static com.github.dfauth.ta.util.BigDecimalOps.divide;
import static java.math.BigDecimal.ZERO;
import static java.util.function.Function.identity;
import static java.util.function.Predicate.not;

@Slf4j
public class Collectors {

    public static <T,K,V> Collector<T,?,Map<K,V>> toTreeMap(Comparator<? super K> c, BiConsumer<Map<K,V>,T> consumer) {
        Map<K,V> initial = new TreeMap<>(c);
        BiFunction<Map<K, V>, T, Map<K, V>> accumulator = (m,t) -> {
            consumer.accept(m,t);
            return m;
        };
        return reducer(initial, accumulator, oops());
    }

    public static <T,U> Collector<T,?,U> reducer(U initial, BiFunction<U,T,U> accumulator, BinaryOperator<U> combiner) {

        AtomicReference<U> ref = new AtomicReference<>(initial);
        return new Collector<T, AtomicReference<U>, U>() {
            @Override
            public Supplier<AtomicReference<U>> supplier() {
                return () -> ref;
            }

            @Override
            public BiConsumer<AtomicReference<U>, T> accumulator() {
                return (r,t) -> ref.set(accumulator.apply(r.get(), t));
            }

            @Override
            public BinaryOperator<AtomicReference<U>> combiner() {
                return (l,r) -> {
                    ref.set(combiner.apply(l.get(), r.get()));
                    return ref;
                };
            }

            @Override
            public Function<AtomicReference<U>, U> finisher() {
                return AtomicReference::get;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Set.of();
            }
        };
    }

    public static <K,V> Collector<Map.Entry<K,V>,?,Map<K,V>> toMapEntry() {
        return toMapEntry(Map::entry);
    }

    public static <K,V,T,R> Collector<Map.Entry<K,V>,?,Map<T,R>> toMapEntry(BiFunction<K,V,Map.Entry<T,R>> mapper) {
        return java.util.stream.Collectors.toMap(e -> mapper.apply(e.getKey(),e.getValue()).getKey(), e -> mapper.apply(e.getKey(),e.getValue()).getValue());
    }

    public static <T,R> Function<List<T>, Optional<Tuple2<Optional<R>,T>>> zipWithMostRecent(Function<List<T>,Optional<R>> f) {
        return l -> last(l).map(t -> tuple2(f.apply(l),t));
    }

    public static final Function<List<BigDecimal>, Optional<BigDecimal>> SMA = l -> nonEmpty(l)
            .map(_l -> divide(_l.stream().reduce(ZERO,BigDecimal::add), _l.size()));

    public static final Function<List<BigDecimal>, Optional<BigDecimal>> EMA = ema(2);

    public static final Function<List<BigDecimal>, Optional<BigDecimal>> ema(int smoothingFactor) {
        return l -> {
            Optional<BigDecimal> firstValue = SMA.apply(l);
            BigDecimal weight = BigDecimal.valueOf(((double) smoothingFactor) / (1 + l.size()));
            return firstValue.map(fv -> l.stream()
                    .reduce(fv,
                            (ema, v) -> v.multiply(weight).add(ema.multiply(ONE3.subtract(weight))),
                            oops()
                    ));
        };
    }

    public static <T> Collector<T, Stack<T>,List<T>> comparing(BinaryOperator<T> f2) {

        return new Collector<>() {
            @Override
            public Supplier<Stack<T>> supplier() {
                return Stack::new;
            }

            @Override
            public BiConsumer<Stack<T>, T> accumulator() {
                return (stack,t) -> stack.push(Optional.of(stack)
                        .filter(not(Stack::empty))
                        .map(Stack::peek)
                        .map(_t -> f2.apply(_t,t))
                        .orElse(t));
            }

            @Override
            public BinaryOperator<Stack<T>> combiner() {
                return oops();
            }

            @Override
            public Function<Stack<T>, List<T>> finisher() {
                return ArrayList::new;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Set.of();
            }
        };
    }

    public static <T,R> Collector<T, ?,List<R>> consecutive(BiFunction<T,T,R> f2) {
        return consecutive(acc -> (l,r) -> {
            acc.add(f2.apply(l,r));
        });
    }

    public static <T,R> Collector<T, ?,List<R>> consecutive(Function<List<R>, BiConsumer<T,T>> f2) {
        List<R> list = new ArrayList<>();
        return Consecutive.consecutive(list, l -> (left,right) -> {
            f2.apply(l).accept(left, right);
            return l;
        });
    }
    public static <U> BinaryOperator<U> oops() {
        return (t1,t2) -> {
            throw new IllegalStateException("Oops. Parallel operations not supported");
        };
    }

    public static <T,R,S> Collector<T, Map<R,S>,Map<R,S>> toMap(Function<T,R> keyMapper,Function<T,BiFunction<R,S,S>> reMapper) {
        return new Collector<>() {

            @Override
            public Supplier<Map<R, S>> supplier() {
                return HashMap::new;
            }

            @Override
            public BiConsumer<Map<R, S>, T> accumulator() {
                return (m,t) -> m.compute(keyMapper.apply(t),reMapper.apply(t));
            }

            @Override
            public BinaryOperator<Map<R, S>> combiner() {
                return oops();
            }

            @Override
            public Function<Map<R, S>, Map<R, S>> finisher() {
                return identity();
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Set.of();
            }
        };
    }

}
