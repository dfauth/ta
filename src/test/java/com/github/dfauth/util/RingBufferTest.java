package com.github.dfauth.util;

import com.github.dfauth.ta.functional.Lists;
import com.github.dfauth.ta.util.ArrayRingBuffer;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.dfauth.ta.functional.ReducersTest.bdRange;
import static com.github.dfauth.ta.functional.StatefulFunctions.roc;
import static com.github.dfauth.ta.functional.StatefulFunctions.sma;
import static org.junit.Assert.*;

@Slf4j
public class RingBufferTest {

    @Test
    public void testSanity() {
        {
            Optional<BigDecimal> r = Stream.of(1).map(BigDecimal::valueOf).reduce(BigDecimal::subtract);
            assertEquals(BigDecimal.valueOf(1), r.get());
        }
        {
            Optional<BigDecimal> r = Stream.of(1,2).map(BigDecimal::valueOf).reduce(BigDecimal::subtract);
            assertEquals(BigDecimal.valueOf(-1), r.get());
        }
        {
            Optional<BigDecimal> r = Stream.of(1,2,3).map(BigDecimal::valueOf).reduce(BigDecimal::subtract);
            assertEquals(BigDecimal.valueOf(-4), r.get());
        }
    }

//    @Test
//    public void testReduce() {
//        Optional<BigDecimal> r = Stream.of(1).map(BigDecimal::valueOf).collect(with(WindowReducers.roc()));
//        assertEquals(Optional.empty(), r);
//    }

    @Test
    public void testIt() {
        ArrayRingBuffer<Integer> buffer = new ArrayRingBuffer<>(4);
        assertFalse(buffer.isFull());
        assertEquals(4, buffer.capacity());
        assertEquals(0, buffer.size());
        assertEquals(List.of(), buffer.stream().collect(Collectors.toList()));
        buffer.add(1);
        assertFalse(buffer.isFull());
        assertEquals(4, buffer.capacity());
        assertEquals(1, buffer.size());
        assertEquals(List.of(1), buffer.stream().collect(Collectors.toList()));
        buffer.add(2);
        assertFalse(buffer.isFull());
        assertEquals(4, buffer.capacity());
        assertEquals(2, buffer.size());
        assertEquals(List.of(1,2), buffer.stream().collect(Collectors.toList()));
        buffer.add(3);
        assertFalse(buffer.isFull());
        assertEquals(4, buffer.capacity());
        assertEquals(3, buffer.size());
        assertEquals(List.of(1,2,3), buffer.stream().collect(Collectors.toList()));
        buffer.add(4);
        assertTrue(buffer.isFull());
        assertEquals(4, buffer.capacity());
        assertEquals(4, buffer.size());
        assertEquals(List.of(1,2,3,4), buffer.stream().collect(Collectors.toList()));
        buffer.add(5);
        assertTrue(buffer.isFull());
        assertEquals(4, buffer.capacity());
        assertEquals(4, buffer.size());
        assertEquals(List.of(2,3,4,5), buffer.stream().collect(Collectors.toList()));
    }

    @Test
    public void testRoC() {
        assertEquals(
                List.of(bd(1),bd(1),bd(1),bd(1)),
                bdRange(0,5).map(roc()).flatMap(Optional::stream).collect(Collectors.toList())
        );
    }

    @Test
    public void testSma() {
        assertEquals(
                Lists.of(1).map(BigDecimal::valueOf),
                bdRange(0,5).map(roc()).flatMap(Optional::stream).map(sma(4)).flatMap(Optional::stream).collect(Collectors.toList())
        );
        Function<BigDecimal, BigDecimal> doubler = bd -> bd.multiply(BigDecimal.valueOf(2));
        assertEquals(
                Lists.of(2,2,2,2,2,2).map(BigDecimal::valueOf),
                bdRange(0,10).map(doubler).map(roc()).flatMap(Optional::stream).map(sma(4)).flatMap(Optional::stream).collect(Collectors.toList())
        );
    }

    private BigDecimal bd(int i) {
        return BigDecimal.valueOf(i);
    }
}
