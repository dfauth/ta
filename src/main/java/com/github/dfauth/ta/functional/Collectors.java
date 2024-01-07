package com.github.dfauth.ta.functional;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;

import static com.github.dfauth.ta.functional.Tuple2.tuple2;
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


}
