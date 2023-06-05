package com.github.dfauth.ta.util;

import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class RingBuffer<T> {

    private final T[] buffer;
    private int current = 0;

    public RingBuffer(Supplier<T[]> arrayInitialiser) {
        this.buffer = arrayInitialiser.get();
    }

    public Optional<T> add(T t) {
        int offset = current % this.buffer.length;
        Optional<T> removed;
        if(isFull()) {
            removed = Optional.ofNullable(this.buffer[offset]);
        } else {
            removed = Optional.empty();
        }
        this.buffer[offset] = t;
        current++;
        return removed;
    }

    public boolean isFull() {
        return current >= this.buffer.length;
    }

    public int capacity() {
        return Math.min(current, this.buffer.length);
    }

    public Stream<T> stream() {
        final int[] i = {0};
        Iterator<? extends T> it = new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return i[0] < capacity();
            }

            @Override
            public T next() {
                return (T) buffer[(current+i[0]++)%buffer.length];
            }
        };
        return StreamSupport.stream(Spliterators.spliterator(it, this.buffer.length, Spliterator.NONNULL), false);
    }
}
