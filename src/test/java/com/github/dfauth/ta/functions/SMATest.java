package com.github.dfauth.ta.functions;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import static com.github.dfauth.ta.functions.MovingAverages.sma;
import static com.github.dfauth.ta.functions.RateOfChange.roc;
import static org.junit.Assert.assertEquals;

public class SMATest {

    private static final int[] TEST_DATA = new int[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
    private static final double[] adt_prices = new double[]{3.80,3.81,3.77, 3.72, 3.68,3.60,3.51,3.71};

    @Test
    public void testIt() {
        assertEquals(
                List.of(3.5, 4.5, 5.5, 6.5, 7.5, 8.5, 9.5, 10.5, 11.5, 12.5, 13.5, 14.5),
                IntStream.of(TEST_DATA).boxed()
                        .map(sma(4, Accumulator.INT_ACCUMULATOR.get()))
                        .flatMap(Optional::stream)
                        .collect(Collectors.toList()));
    }

    @Test
    public void testASX_ADT_sma() {
        assertEquals(
                List.of(3.716, 3.656, 3.644).stream().map(BigDecimal::valueOf).collect(Collectors.toList()),
                DoubleStream.of(adt_prices).boxed()
                        .map(BigDecimal::valueOf)
                        .map(sma(5))
                        .flatMap(Optional::stream)
                        .collect(Collectors.toList()));
    }

    @Test
    public void testASX_ADT_roc() {
        assertEquals(
                List.of(0.003, -0.010, -0.013, -0.011, -0.022, -0.025, 0.057).stream()
                        .map(BigDecimal::valueOf)
                        .map(bd -> bd.setScale(3))
                        .collect(Collectors.toList()),
                DoubleStream.of(adt_prices).boxed()
                        .map(BigDecimal::valueOf)
                        .map(roc())
                        .flatMap(Optional::stream)
                        .collect(Collectors.toList()));
    }

    @Test
    public void testASX_ADT_roc_sma() {
        assertEquals(
                List.of(-0.016, -0.003).stream().map(BigDecimal::valueOf).collect(Collectors.toList()),
                DoubleStream.of(adt_prices).boxed()
                        .map(BigDecimal::valueOf)
                        .map(roc())
                        .flatMap(Optional::stream)
                        .map(sma(5))
                        .flatMap(Optional::stream)
                        .collect(Collectors.toList()));
    }
}
