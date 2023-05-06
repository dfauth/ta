package com.github.dfauth.ta.functions;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.dfauth.ta.functions.MovingAverages.ema;
import static com.github.dfauth.ta.functions.RateOfChange.roc;
import static org.junit.Assert.assertEquals;

public class EMATest {

    private static final int[] TEST_DATA = new int[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
    private static final double[] adt_prices = new double[]{3.80,3.81,3.77, 3.72, 3.68,3.60,3.51,3.71};

    @Test
    public void testIt() {
        assertEquals(
                 Stream.concat(Stream.of(BigDecimal.valueOf(3)), Stream.of(3.8, 4.68, 5.608, 6.5648, 7.53888, 8.523328, 9.5139968, 10.50839808, 11.505038848, 12.5030233088, 13.50181398528, 14.501088391168).map(BigDecimal::valueOf)).collect(Collectors.toList()),
                IntStream.of(TEST_DATA).boxed()
                        .map(BigDecimal::valueOf)
                        .map(ema(4))
                        .flatMap(Optional::stream)
                        .collect(Collectors.toList()));
    }

    @Test
    public void testASX_ADT_ema() {
        assertEquals(
                List.of(3.760, 3.707, 3.641, 3.664).stream().map(BigDecimal::valueOf).map(e -> e.setScale(3)).collect(Collectors.toList()),
                DoubleStream.of(adt_prices).boxed()
                        .map(BigDecimal::valueOf)
                        .map(ema(5))
                        .flatMap(Optional::stream)
                        .map(e -> e.setScale(3, RoundingMode.HALF_UP))
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
    public void testASX_ADT_roc_ema() {
        assertEquals(
                List.of(-0.011, -0.016, 0.009).stream().map(BigDecimal::valueOf).map(e -> e.setScale(3)).collect(Collectors.toList()),
                DoubleStream.of(adt_prices).boxed()
                        .map(BigDecimal::valueOf)
                        .map(roc())
                        .flatMap(Optional::stream)
                        .map(ema(5))
                        .flatMap(Optional::stream)
                        .map(e -> e.setScale(3, RoundingMode.HALF_UP))
                        .collect(Collectors.toList()));
    }
}
