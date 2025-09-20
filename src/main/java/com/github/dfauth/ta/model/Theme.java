package com.github.dfauth.ta.model;

import com.github.dfauth.ta.functional.Predicates;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.*;
import java.util.stream.Collector;

public enum Theme implements Predicate<Position> {

    MOMENTUM,
    MEAN_REVERSION,
    COMPOUNDER,
    DIVIDEND,
    CASH;

    public static Predicate<Position> fromString(String theme) {
        return Predicates.fromString(theme, t -> Optional.ofNullable(Theme.allMatch(t)));
//        return isNegated(theme, p -> test(p, theme::equals));
//        return stream(values())
//                .filter(v -> theme.contains(v.name()))
//                .map(v -> theme.startsWith("!") ?
//                        predicate(t -> !t.equals(v)) :
//                        predicate(v))
//                .findFirst()
//                .orElse(alwaysTrue());
    }

    private static Theme allMatch(Position p) {
        List<Theme> result = p.getTrades().stream().map(Trade::getTheme).filter(Objects::nonNull).distinct().toList();
        if(result.isEmpty()) {
            return null;
        } else if (result.size() == 1) {
            return result.get(0);
        } else {
            throw new IllegalStateException("unexpected values: "+result);
        }
    }

    @Override
    public boolean test(Position position) {
        return test(position, this::equals);
    }

    public static boolean test(Position position, Predicate<Theme> p) {
        return position.getTrades().stream().map(Trade::getTheme).allMatch(p);
    }

    public static <T> Collector<T, ?, T> assertOne() {
        return assertOne(java.util.stream.Collectors.<T>toList(), l -> {
            assert l.size() == 1;
            return l.get(0);
        });
    }

    public static <T, A, R, S> Collector<T, A, S> assertOne(Collector<T, A, R> downstream, Function<R, S> finisher) {
        return new Collector<>() {
            @Override
            public Supplier<A> supplier() {
                return downstream.supplier();
            }

            @Override
            public BiConsumer<A, T> accumulator() {
                return downstream.accumulator();
            }

            @Override
            public BinaryOperator<A> combiner() {
                return downstream.combiner();
            }

            @Override
            public Function<A, S> finisher() {
                return downstream.finisher().andThen(finisher);
            }


            @Override
            public Set<Characteristics> characteristics() {
                return downstream.characteristics();
            }
        };
    }

}
