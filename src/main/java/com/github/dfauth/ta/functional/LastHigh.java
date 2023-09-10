package com.github.dfauth.ta.functional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import static com.github.dfauth.ta.functional.Tuple2.tuple2;
import static java.util.function.Predicate.not;

public class LastHigh {

    public static Optional<BigDecimal> pctBelow(List<BigDecimal> input) {
        Iterator<BigDecimal> it = input.iterator();
        AtomicReference<BigDecimal> max = new AtomicReference<>(null);
        input.stream()
                .filter(p -> Optional.ofNullable(max.get()).map(_max -> _max.compareTo(p) < 0).orElse(true))
                .forEach(max::set);
        return Optional.ofNullable(max.get())
                .flatMap(h -> Optional.of(input)
                        .filter(not(List::isEmpty))
                        .map(l -> l.get(input.size()-1)
                                .subtract(h).divide(h, RoundingMode.HALF_UP)));
    }
}
