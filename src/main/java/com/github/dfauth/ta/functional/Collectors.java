package com.github.dfauth.ta.functional;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;

import static com.github.dfauth.ta.functional.Lists.last;
import static com.github.dfauth.ta.functional.Lists.nonEmpty;
import static com.github.dfauth.ta.functional.Tuple2.tuple2;
import static com.github.dfauth.ta.util.BigDecimalOps.ONE3;
import static com.github.dfauth.ta.util.BigDecimalOps.divide;
import static java.math.BigDecimal.ZERO;
import static java.util.function.Predicate.not;

@Slf4j
public class Collectors {

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
