package com.github.dfauth.util;

import com.github.dfauth.ta.util.RingBuffer;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.dfauth.ta.functions.RateOfChange.roc;
import static org.junit.Assert.*;

@Slf4j
public class RingBufferTest {

    @Test
    public void testIt() {
        RingBuffer<Integer> buffer = new RingBuffer<>(() -> new Integer[4]);
        assertFalse(buffer.isFull());
        assertEquals(0, buffer.capacity());
        buffer.add(1);
        assertFalse(buffer.isFull());
        assertEquals(1, buffer.capacity());
        buffer.add(2);
        assertFalse(buffer.isFull());
        assertEquals(2, buffer.capacity());
        buffer.add(3);
        assertFalse(buffer.isFull());
        assertEquals(3, buffer.capacity());
        buffer.add(4);
        assertTrue(buffer.isFull());
        assertEquals(4, buffer.capacity());
        buffer.add(5);
        assertTrue(buffer.isFull());
        assertEquals(4, buffer.capacity());
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
}
