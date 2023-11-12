package com.github.dfauth.ta.util;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ArrayRingBuffer<T> implements RingBuffer<T> {

    public static RingBuffer<BigDecimal> create(int capacity) {
        return new ArrayRingBuffer<>(new BigDecimal[capacity]);
    }

    private final T[] buffer;
    private final AtomicInteger writeCounter = new AtomicInteger(0);
    private final AtomicInteger readCounter = new AtomicInteger(0);

    public ArrayRingBuffer(T... buffer) {
        this.buffer = buffer;
    }

    public int write(T t) {
        this.buffer[offset(writeCounter.getAndIncrement())] = t;
        return capacity() - size();
    }

    public T read() {
        if(readCounter.get() >= writeCounter.get()) {
            throw new IllegalStateException("Nothing to read");
        }
        return this.buffer[offset(readCounter.getAndIncrement())];
    }

    private int offset(int i) {
        return i % capacity();
    }

    public boolean isFull() {
        return writeCounter.get() >= capacity();
    }

    public int capacity() {
        return this.buffer.length;
    }

    public int size() {
        return Math.min(writeCounter.get(), capacity());
    }

    public Iterator<T> iterator() {

        T[] tmp = Arrays.copyOf(buffer, buffer.length);
        AtomicInteger i = new AtomicInteger(0);
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return i.get()<size();
            }

            @Override
            public T next() {
                return isFull() ?
                        tmp[offset(writeCounter.get()+i.getAndIncrement())] :
                        tmp[offset(0+i.getAndIncrement())];
            }
        };
    }

    @Override
    public Stream<T> stream() {
        return StreamSupport.stream(Spliterators.spliterator(
                iterator(),
                this.size(),
                Spliterator.NONNULL), false);
    }

}
