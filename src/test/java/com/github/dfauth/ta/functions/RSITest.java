package com.github.dfauth.ta.functions;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import static com.github.dfauth.ta.functions.RSI.rsi;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RSITest {

    private static final double[] adt_prices = new double[]{3.80,3.81,3.77, 3.72, 3.68,3.60,3.51,3.71};

    @Test
    public void testIt() {
        assertEquals(
                DoubleStream.of(2.013,38.350).mapToObj(BigDecimal::valueOf).map(bd -> bd.setScale(3)).collect(Collectors.toList()),
                DoubleStream.of(adt_prices).boxed()
                        .map(BigDecimal::valueOf)
                        .map(rsi(7))
                        .flatMap(Optional::stream)
                        .collect(Collectors.toList())
        );
    }

}
