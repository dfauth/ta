package com.github.dfauth.ta.util;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class RingBuffer<T> {

    private final AtomicReference<T>[] buffer;
    private final AtomicInteger current = new AtomicInteger(0);

    public static <T,R> Function<T,Optional<R>> windowfy(int period, Function<List<T>, Optional<R>> f) {
        return new RingBuffer<T>(period).map(f);
    }

    public RingBuffer(int capacity) {
        this.buffer = IntStream.range(0,capacity).mapToObj(i -> new AtomicReference(null)).collect(Collectors.toList()).toArray(AtomicReference[]::new);
    }

    public Optional<T> add(T t) {
        return Optional.ofNullable(this.buffer[current.getAndIncrement() % capacity()].getAndSet(t));
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
        AtomicInteger i = new AtomicInteger(offset());
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return i.get() <= size();
            }

            @Override
            public T next() {
                return get(offset(i.getAndIncrement()));
            }
        };
    }

    public Stream<T> stream() {
        return StreamSupport.stream(Spliterators.spliterator(iterator(), this.buffer.length, Spliterator.NONNULL), false);
    }

    public List<T> toList() {
        return stream().collect(Collectors.toList());
    }

    public <R> Function<T,Optional<R>> map(Function<List<T>,Optional<R>> f) {
        return t -> {
            add(t);
            return f.apply(toList());
        };
    }

}
