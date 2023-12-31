package com.github.dfauth.util;

import com.github.dfauth.ta.util.ArrayRingBuffer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class RingBufferTest {

    @Test
    public void testIt() {
        ArrayRingBuffer<Integer> buffer = new ArrayRingBuffer<>(new Integer[4]);
        assertFalse(buffer.isFull());
        assertEquals(4, buffer.capacity());
        assertEquals(0, buffer.size());
        assertThrows(IllegalStateException.class, buffer::read);
        assertEquals(List.of(), buffer.stream().collect(Collectors.toList()));
        assertEquals(3,buffer.write(1));
        assertFalse(buffer.isFull());
        assertEquals(4, buffer.capacity());
        assertEquals(1, buffer.size());
        assertEquals(1, buffer.read().intValue());
        assertThrows(IllegalStateException.class, buffer::read);
        assertEquals(List.of(1), buffer.stream().collect(Collectors.toList()));
        assertEquals(2,buffer.write(2));
        assertFalse(buffer.isFull());
        assertEquals(4, buffer.capacity());
        assertEquals(2, buffer.size());
        assertEquals(2, buffer.read().intValue());
        assertThrows(IllegalStateException.class, buffer::read);
        assertEquals(List.of(1,2), buffer.stream().collect(Collectors.toList()));
        assertEquals(1,buffer.write(3));
        assertFalse(buffer.isFull());
        assertEquals(4, buffer.capacity());
        assertEquals(3, buffer.size());
        assertEquals(3, buffer.read().intValue());
        assertThrows(IllegalStateException.class, buffer::read);
        assertEquals(List.of(1,2,3), buffer.stream().collect(Collectors.toList()));
        assertEquals(0,buffer.write(4));
        assertTrue(buffer.isFull());
        assertEquals(4, buffer.capacity());
        assertEquals(4, buffer.size());
        assertEquals(4, buffer.read().intValue());
        assertThrows(IllegalStateException.class, buffer::read);
        assertEquals(List.of(1,2,3,4), buffer.stream().collect(Collectors.toList()));
        assertEquals(0,buffer.write(5));
        assertTrue(buffer.isFull());
        assertEquals(4, buffer.capacity());
        assertEquals(4, buffer.size());
        assertEquals(5, buffer.read().intValue());
        assertThrows(IllegalStateException.class, buffer::read);
        assertEquals(List.of(2,3,4,5), buffer.stream().collect(Collectors.toList()));
    }

}
