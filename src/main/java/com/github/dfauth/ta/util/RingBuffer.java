package com.github.dfauth.ta.util;

import com.github.dfauth.ta.functional.SimpleCollector;

import java.util.Optional;
import java.util.stream.Stream;

public interface RingBuffer<T> {

    int capacity();

    int write(T t);

    T read();

    Stream<T> stream();

    boolean isFull();

    default <A,R> Optional<R> calculate(SimpleCollector<T,A,R> f2) {
        return Optional.of(this)
                .filter(RingBuffer::isFull)
                .map(rb -> rb.stream().collect(f2));
    }
}
