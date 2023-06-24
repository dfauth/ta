package com.github.dfauth.util;

import com.github.dfauth.ta.util.ArrayRingBuffer;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.dfauth.ta.functions.RateOfChange.roc;
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
        assertEquals(List.of(), buffer.toCollection());
        buffer.add(1);
        assertFalse(buffer.isFull());
        assertEquals(4, buffer.capacity());
        assertEquals(1, buffer.size());
        assertEquals(List.of(1), buffer.toCollection());
        buffer.add(2);
        assertFalse(buffer.isFull());
        assertEquals(4, buffer.capacity());
        assertEquals(2, buffer.size());
        assertEquals(List.of(1,2), buffer.toCollection());
        buffer.add(3);
        assertFalse(buffer.isFull());
        assertEquals(4, buffer.capacity());
        assertEquals(3, buffer.size());
        assertEquals(List.of(1,2,3), buffer.toCollection());
        buffer.add(4);
        assertTrue(buffer.isFull());
        assertEquals(4, buffer.capacity());
        assertEquals(4, buffer.size());
        assertEquals(List.of(1,2,3,4), buffer.toCollection());
        buffer.add(5);
        assertTrue(buffer.isFull());
        assertEquals(4, buffer.capacity());
        assertEquals(4, buffer.size());
        assertEquals(List.of(2,3,4,5), buffer.toCollection());
    }

    @Test
    public void testRoC() {
        Function<BigDecimal, Optional<BigDecimal>> x = roc(4);
        assertEquals(Optional.empty(), x.apply(BigDecimal.valueOf(1).setScale(3)));
        assertEquals(Optional.empty(), x.apply(BigDecimal.valueOf(2).setScale(3)));
        assertEquals(Optional.empty(), x.apply(BigDecimal.valueOf(3).setScale(3)));
        assertEquals(Optional.of(BigDecimal.valueOf(2.5).setScale(3)), x.apply(BigDecimal.valueOf(4).setScale(3)));
        assertEquals(Optional.of(BigDecimal.valueOf(3.5).setScale(3)), x.apply(BigDecimal.valueOf(5).setScale(3)));
    }

//    @Test
//    public void testList() {
//
//        Reducer<Integer, BigDecimal> difference = c -> {
//            AtomicReference<BigDecimal> previous = new AtomicReference<>(null);
//            BigDecimal TWO = BigDecimal.valueOf(2);
//            List<BigDecimal> diffs = c.stream().map(BigDecimal::valueOf)
//                    .map(bd -> {
//                        BigDecimal _prev = previous.getAndSet(bd);
//                        return Optional.ofNullable(_prev).map(_p -> _p.subtract(bd).divide(TWO, RoundingMode.HALF_UP));
//                    })
//                    .flatMap(Optional::stream)
//                    .collect(Collectors.toList());
//            return Optional.of(diffs).filter(not(List::isEmpty)).map(l -> l.get(l.size()-1));
//        };
//
//        Function<BigDecimal, Optional<BigDecimal>> g = Reducer.windowfy(4, Reducer.roc());
//        List<BigDecimal> result = IntStream.range(0, 4).mapToObj(BigDecimal::valueOf).map(g).flatMap(Optional::stream).collect(Collectors.toList());
//        assertEquals(List.of(bd(1),bd(1),bd(1)), result);
//    }

    private BigDecimal bd(int i) {
        return BigDecimal.valueOf(i);
    }
}
