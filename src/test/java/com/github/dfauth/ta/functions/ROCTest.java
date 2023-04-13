package com.github.dfauth.ta.functions;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.dfauth.ta.functions.RateOfChange.roc;
import static org.junit.Assert.assertEquals;

public class ROCTest {

    private static final int[] TEST_DATA = new int[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
    private static final BigDecimal ONE_3DP = BigDecimal.valueOf(1).setScale(3);

    @Test
    public void testIt() {
        assertEquals(
                Stream.of(1.000, 0.500, 0.333, 0.250, 0.200, 0.167, 0.143, 0.125, 0.111, 0.100, 0.091, 0.083, 0.077, 0.071, 0.067).map(BigDecimal::valueOf).map(bd -> bd.setScale(3)).collect(Collectors.toList()),
                IntStream.of(TEST_DATA).boxed()
                        .map(BigDecimal::valueOf)
                        .map(bd -> bd.setScale(3))
                        .map(roc())
                        .flatMap(Optional::stream)
                        .collect(Collectors.toList())
        );
    }
}
