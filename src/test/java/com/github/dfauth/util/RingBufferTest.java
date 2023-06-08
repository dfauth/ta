package com.github.dfauth.util;

import com.github.dfauth.ta.util.RingBuffer;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.github.dfauth.ta.functions.RateOfChange.roc;
import static com.github.dfauth.ta.util.TryCatch.tryFinally;
import static org.junit.Assert.*;

@Slf4j
public class RingBufferTest {

    @Test
    public void testIt() {
        RingBuffer<Integer> buffer = new RingBuffer<>(4);
        assertFalse(buffer.isFull());
        assertEquals(4, buffer.capacity());
        assertEquals(0, buffer.size());
        buffer.add(1);
        assertFalse(buffer.isFull());
        assertEquals(4, buffer.capacity());
        assertEquals(1, buffer.size());
        buffer.add(2);
        assertFalse(buffer.isFull());
        assertEquals(4, buffer.capacity());
        assertEquals(2, buffer.size());
        buffer.add(3);
        assertFalse(buffer.isFull());
        assertEquals(4, buffer.capacity());
        assertEquals(3, buffer.size());
        buffer.add(4);
        assertTrue(buffer.isFull());
        assertEquals(4, buffer.capacity());
        assertEquals(4, buffer.size());
        buffer.add(5);
        assertTrue(buffer.isFull());
        assertEquals(4, buffer.capacity());
        assertEquals(4, buffer.size());
        assertEquals(List.of(2,3,4,5), buffer.stream().collect(Collectors.toList()));
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

    @Test
    public void testList() {
        RingBuffer<Integer> buffer = new RingBuffer<>(4);
//        List<Integer> l = buffer.toList();
        IntStream.range(1,buffer.capacity()).forEach(buffer::add);
        final AtomicReference<BigDecimal> previous = new AtomicReference<>(null);
        Function<Integer, Optional<BigDecimal>> f = i -> {
            return Optional.ofNullable(previous.get()).map(p ->
                tryFinally(() -> BigDecimal.valueOf(i).subtract(p), () -> previous.set(BigDecimal.valueOf(i)))
            );
        };
        RingBuffer<BigDecimal> buffer1 = buffer.stream().reduce(
                new RingBuffer<>(buffer.capacity()),
                (rb,t) -> {
                    f.apply(t).ifPresent(rb::add);
                    return rb;
                },
                (rb1, rb2) -> {
                    throw new UnsupportedOperationException("Oops");
                }
        );
//        buffer.map(roc());
    }
}
