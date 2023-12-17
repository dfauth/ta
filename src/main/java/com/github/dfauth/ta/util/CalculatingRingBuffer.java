package com.github.dfauth.ta.util;

import com.github.dfauth.ta.functional.SimpleCollector;

import java.util.Optional;
import java.util.stream.Stream;

public class CalculatingRingBuffer<T,A,R> implements RingBuffer<T> {

    private final RingBuffer<T> ringBuffer;
    private final SimpleCollector<T, A, R> f;

    public CalculatingRingBuffer(RingBuffer<T> ringBuffer, SimpleCollector<T,A,R> f) {
        this.ringBuffer = ringBuffer;
        this.f = f;
    }


    @Override
    public int capacity() {
        return ringBuffer.capacity();
    }

    @Override
    public int write(T t) {
        return ringBuffer.write(t);
    }

    @Override
    public T read() {
        return ringBuffer.read();
    }

    @Override
    public Stream<T> stream() {
        return ringBuffer.stream();
    }

    @Override
    public boolean isFull() {
        return ringBuffer.isFull();
    }

    public Optional<R> calculate() {
        return ringBuffer.collect(f);
    }

    public static <T,A,R> CalculatingRingBuffer<T,A,R> create(RingBuffer<T> ringBuffer, SimpleCollector<T,A,R> paf) {
        return new CalculatingRingBuffer<>(ringBuffer, paf);
    }
}
