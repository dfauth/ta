package com.github.dfauth.ta.functional;

import com.github.dfauth.ta.util.ArrayRingBuffer;
import com.github.dfauth.ta.util.RingBuffer;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;
import java.util.stream.Collector;

import static com.github.dfauth.ta.functional.Tuple2.tuple2;
import static java.util.function.Function.identity;
import static java.util.function.Predicate.not;

@Slf4j
public class Collectors {

    public static final Function<List<BigDecimal>, BigDecimal> SMA = l -> BigDecimal.valueOf(l.stream().mapToDouble(BigDecimal::doubleValue).sum() / l.size());

    public static final Function<List<BigDecimal>, BigDecimal> EMA = ema(2);

    public static final Function<List<BigDecimal>, BigDecimal> ema(int smoothingFactor) {
        return l -> {
            double firstValue = SMA.apply(l).doubleValue();
            double weight = ((double) smoothingFactor)/(1+l.size());
            return BigDecimal.valueOf(l.stream()
                    .map(BigDecimal::doubleValue)
                    .reduce(firstValue,
                            (ema, v) -> v*weight + ema * (1-weight),
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

    public static <T,R> Collector<T, Stack<Tuple2<T, Optional<R>>>,List<R>> adjacent(BiFunction<T,T,R> f2) {
        return new Collector<>() {
            @Override
            public Supplier<Stack<Tuple2<T, Optional<R>>>> supplier() {
                return Stack::new;
            }

            @Override
            public BiConsumer<Stack<Tuple2<T, Optional<R>>>, T> accumulator() {
                return (stack,t) -> stack.push(Optional.of(stack)
                        .filter(not(Stack::empty))
                        .map(Stack::peek)
                        .map(Tuple2::_1)
                        .map(_t -> tuple2(t, Optional.of(f2.apply(_t,t))))
                        .orElse(tuple2(t,Optional.empty())));
            }

            @Override
            public BinaryOperator<Stack<Tuple2<T, Optional<R>>>> combiner() {
                return oops();
            }

            @Override
            public Function<Stack<Tuple2<T, Optional<R>>>, List<R>> finisher() {
                return stack -> stack.stream().map(Tuple2::_2).flatMap(Optional::stream).collect(java.util.stream.Collectors.toList());
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Set.of();
            }
        };
    }
    public static <U> BinaryOperator<U> oops() {
        return (t1,t2) -> {
            throw new IllegalStateException("Oops. Parallel operations not supported");
        };
    }


    public static <T> Collector<T, RingBuffer<T>,List<T>> ringBufferCollector(T[] buffer) {
        return ringBufferCollector(buffer, identity());
    }

    public static <T,R> Collector<T, RingBuffer<T>,R> ringBufferCollector(T[] buffer, Function<List<T>,R> finisher) {
        return mutableCollector(
                new ArrayRingBuffer<>(buffer),
                (_rb,t) -> {
                    _rb.write(t);
                    return _rb;
                },
                _rb -> {
                    List<T> l = _rb.stream().collect(java.util.stream.Collectors.toList());
                    return finisher.apply(l);
                }
        );
    }

    public static <T,A> Collector<T, AtomicReference<A>,A> collector(A initial, BiFunction<A,T,A> accumulator) {
        return collector(initial, accumulator, identity());
    }

    public static <T,A,R> Collector<T, AtomicReference<A>,R> collector(A initial, BiFunction<A,T,A> accumulator, Function<A,R> finisher) {
        return new Collector<>() {
            @Override
            public Supplier<AtomicReference<A>> supplier() {
                return () -> new AtomicReference<>(initial);
            }

            @Override
            public BiConsumer<AtomicReference<A>, T> accumulator() {
                return (a,t) -> a.set(accumulator.apply(a.get(), t));
            }

            @Override
            public BinaryOperator<AtomicReference<A>> combiner() {
                return (a1,a2) -> {
                    throw new IllegalStateException("Parallel operations not supported");
                };
            }

            @Override
            public Function<AtomicReference<A>, R> finisher() {
                return a -> finisher.apply(a.get());
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Set.of();
            }
        };
    }

    public static <T,A,R> Collector<T, A,R> mutableCollector(A initial, BiFunction<A,T,A> accumulator, Function<A,R> finisher) {
        return new Collector<>() {
            @Override
            public Supplier<A> supplier() {
                return () -> initial;
            }

            @Override
            public BiConsumer<A, T> accumulator() {
                return accumulator::apply;
            }

            @Override
            public BinaryOperator<A> combiner() {
                return (a1,a2) -> {
                    throw new IllegalStateException("Parallel operations not supported");
                };
            }

            @Override
            public Function<A, R> finisher() {
                return finisher;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Set.of();
            }
        };
    }
}
