package com.github.dfauth.ta.functions;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import static com.github.dfauth.ta.functions.RSI.rsi;
import static org.junit.Assert.assertEquals;

public class RSITest {

    private static final double[] adt_prices = new double[]{3.80,3.81,3.77, 3.72, 3.68,3.60,3.51,3.71};

    @Test
    public void testIt() {
        assertEquals(
                IntStream.of(33, 20, 15, 10, 7, 7).mapToObj(BigDecimal::valueOf).collect(Collectors.toList()),
                DoubleStream.of(adt_prices).boxed()
                        .map(BigDecimal::valueOf)
                        .map(rsi(7))
                        .flatMap(Optional::stream)
                        .collect(Collectors.toList())
        );
    }

}
