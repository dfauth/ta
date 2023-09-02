package com.github.dfauth.ta.functional;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

import static com.github.dfauth.ta.functional.Tuple2.tuple2;

public class DaysSince {

    public static Optional<Integer> lastHigh(List<BigDecimal> input) {
        Iterator<BigDecimal> it = input.iterator();
        AtomicReference<Tuple2<Integer, BigDecimal>> max = new AtomicReference<>(null);
        IntStream.range(0,input.size())
                .mapToObj(i -> tuple2(input.size()-1-i, it.next()))
                .filter(t2 -> Optional.ofNullable(max.get()).map(_m -> _m._2().compareTo(t2._2()) < 0).orElse(true))
                .forEach(max::set);
        return Optional.ofNullable(max.get()).map(Tuple2::_1);
    }
}
