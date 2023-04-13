package com.github.dfauth.ta.functions;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.github.dfauth.ta.functions.MovingAverages.sma;
import static com.github.dfauth.ta.functions.RateOfChange.roc;
import static org.junit.Assert.assertEquals;

public class ROCTest {

    private static final int[] TEST_DATA = new int[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
    private static final BigDecimal ONE_3DP = BigDecimal.valueOf(1).setScale(3);

    @Test
    public void testIt() {
        assertEquals(
                List.of(ONE_3DP, ONE_3DP, ONE_3DP, ONE_3DP, ONE_3DP, ONE_3DP, ONE_3DP, ONE_3DP, ONE_3DP, ONE_3DP, ONE_3DP, ONE_3DP, ONE_3DP, ONE_3DP, ONE_3DP),
                IntStream.of(TEST_DATA).boxed()
                        .map(BigDecimal::valueOf)
                        .map(roc())
                        .flatMap(Optional::stream)
                        .collect(Collectors.toList())
        );
    }
}
