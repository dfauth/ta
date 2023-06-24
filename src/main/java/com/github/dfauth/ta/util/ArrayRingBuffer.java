package com.github.dfauth.ta.util;

import com.github.dfauth.ta.functional.WindowReducer;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

public class ArrayRingBuffer<T> implements RingBuffer<T> {

    private final AtomicReference<T>[] buffer;
    private final AtomicInteger current = new AtomicInteger(0);

    public static <T,R> Function<T,Optional<R>> windowfy(int period, WindowReducer<T,R> f) {
        return new ArrayRingBuffer<T>(period).map(f);
    }

    public ArrayRingBuffer(int capacity) {
        this.buffer = IntStream.range(0,capacity).mapToObj(i -> new AtomicReference(null)).collect(Collectors.toList()).toArray(AtomicReference[]::new);
    }

    public Optional<T> add(T t) {
        return Optional.ofNullable(this.buffer[offset(current.getAndIncrement())].getAndSet(t));
    }

    public int offset() {
        return offset(current.get());
    }

    private int offset(int i) {
        return i % capacity();
    }

    public T get(int i) {
        return buffer[i].get();
    }

    public boolean isFull() {
        return current.get() >= capacity();
    }

    public int capacity() {
        return this.buffer.length;
    }

    public int size() {
        return Math.min(current.get(), capacity());
    }

    public Iterator<T> iterator() {
        int c = current.get();
        return c <= capacity() ? new Iterator<>() {

            AtomicInteger i = new AtomicInteger(0);

            @Override
            public boolean hasNext() {
                return i.get() < size();
            }

            @Override
            public T next() {
                return get(offset(i.getAndIncrement()));
            }
        } : new Iterator<T>() {

            AtomicInteger i = new AtomicInteger(current.get());

            @Override
            public boolean hasNext() {
                return i.get() < c + capacity();
            }

            @Override
            public T next() {
                return get(offset(i.getAndIncrement()));
            }
        };
    }

    public Collection<T> toCollection() {
        return StreamSupport.stream(Spliterators.spliterator(iterator(), this.buffer.length, Spliterator.NONNULL), false).collect(Collectors.toList());
    }

}
