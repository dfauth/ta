package com.github.dfauth.ta.functional;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.dfauth.ta.functional.RingBufferCollector.ringBufferCollector;
import static com.github.dfauth.ta.functional.Collectors.*;
import static com.github.dfauth.ta.functional.Function2.curry;
import static com.github.dfauth.ta.functional.Lists.mapList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChangeControllerTest {

    public static final List<Integer> INPUT = List.of(1,2,4,8,16);
    public static final BiFunction<Integer,Integer,Integer> increment = (i1, i2) -> i2-i1;
    public static final Function<Integer, Function<Integer,Integer>> curriedIncrement = curry(increment);

    @Test
    public void testIt() {
        List<Function<Integer, Integer>> result = mapList(INPUT, curriedIncrement);
        BiFunction<Integer, Function<Integer, Integer>, Integer> f1 = (i, _f) -> _f.apply(i);
        List<Integer> a = INPUT.subList(1, result.size());
        List<Function<Integer, Integer>> b = result.subList(0, result.size() - 1);
        List<Integer> result1 = Lists.zip(a,b,f1).collect(Collectors.toList());
        assertEquals(List.of(1,2,4,8), result1);
        Optional<BigDecimal> sma = result1.stream().collect(ringBufferCollector(new Integer[3],Lists.<Integer,BigDecimal>mapList(BigDecimal::valueOf).andThen(SMA)));
        assertEquals(4.66667, sma.get().doubleValue(), 0.001d);
    }

    @Test
    public void testItAgain() {
        Optional<BigDecimal> sma = INPUT.stream().collect(adjacent(increment)).stream().collect(ringBufferCollector(new Integer[3],Lists.<Integer,BigDecimal>mapList(BigDecimal::valueOf).andThen(SMA)));
        assertEquals(4.66667, sma.get().doubleValue(), 0.001d);
    }
}