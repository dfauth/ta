package com.github.dfauth.functions;

import com.github.dfauth.ta.functions.MovingAverages;
import com.github.dfauth.ta.functions.TestData;
import com.github.dfauth.ta.model.Price;
import com.github.dfauth.ta.util.BigDecimalOps;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.dfauth.ta.functional.Lists.last;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class MovingAverageTest {

    @Test
    public void testSanity() {

        BinaryOperator<Double> add = Double::sum;
        BiFunction<Double, Double, Double> mult = (d1,d2) -> d1*d2;
        BiFunction<Double, Double, Double> div = (d1,d2) -> d1/d2;
        Function<List<Double>, Optional<Double>> f = MovingAverages.ema(2, add, mult, div);
        assertEquals(2.25, f.apply(List.of(1.0, 2.0, 3.0)).get(), 0.01);
    }

    @Test
    public void testIt() {

        List<BigDecimal> l50 = last(TestData.ALL.stream().map(Price::getClose).collect(Collectors.toList()), 51);
        List<BigDecimal> l20 = last(TestData.ALL.stream().map(Price::getClose).collect(Collectors.toList()), 21);
        BinaryOperator<BigDecimal> add = BigDecimal::add;
        BiFunction<BigDecimal, Double, BigDecimal> mult = (bd,d) -> BigDecimal.valueOf(d).multiply(bd);
        BiFunction<BigDecimal, Double, BigDecimal> div = (bd,d) -> BigDecimalOps.divide(bd,BigDecimal.valueOf(d));
        Function<List<BigDecimal>, Optional<BigDecimal>> f = MovingAverages.ema(2, add, mult, div);
        assertEquals(53.80, f.apply(l20).get().doubleValue(), 0.01);  // 52.85
        assertEquals(52.83, f.apply(l50).get().doubleValue(), 0.01); // 52.3
    }
}
