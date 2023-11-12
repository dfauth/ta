package com.github.dfauth.ta.util;

import java.util.stream.Stream;

public interface RingBuffer<T> {

    int write(T t);

    T read();

    Stream<T> stream();

    boolean isFull();
}
