package com.github.dfauth.ta.functions;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.dfauth.ta.functions.MovingAverages.sma;
import static org.junit.Assert.assertEquals;

public class SMATest {

    private static final int[] TEST_DATA = new int[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};

    @Test
    public void testIt() {
        assertEquals(List.of(0.0, 0.0, 0.0, 0.0, 3.5, 4.5, 5.5, 6.5, 7.5, 8.5, 9.5, 10.5, 11.5, 12.5, 13.5, 14.5), IntStream.of(TEST_DATA).boxed().map(sma(4, Accumulator.INT_ACCUMULATOR)).collect(Collectors.toList()));
    }
}
