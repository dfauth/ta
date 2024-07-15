package com.github.dfauth.ta.util;

import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Stream;

public interface RingBuffer<T> {

    int capacity();

    int write(T t);

    T read();

    Stream<T> stream();

    default Stream<T> streamIfFull() {
        return stream().filter(e -> isFull());
    }

    boolean isFull();

    default <A,R> Optional<R> collect(Collector<T,A,R> f2) {
        return Optional.of(this)
                .filter(RingBuffer::isFull)
                .map(rb -> rb.stream().collect(f2));
    }
}
