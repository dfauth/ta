package com.github.dfauth.ta.functional;

import com.github.dfauth.ta.util.ArrayRingBuffer;
import com.github.dfauth.ta.util.RingBuffer;

import java.util.Collection;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class WindowReducer<T, R> implements WindowFunction<T, R>, Reducer<T, RingBuffer<T>, Optional<R>> {

    private final int capacity;

    public static <T,R> WindowReducer<T,R> windowReducer(int capacity, Function<Collection<T>, Optional<R>> f) {

        return new WindowReducer<>(capacity) {
            @Override
            public Function<RingBuffer<T>, Optional<R>> finisher() {
                return rb -> f.apply(rb.stream().collect(Collectors.toList()));
            }

            @Override
            public BiConsumer<RingBuffer<T>, T> accumulator() {
                return RingBuffer::add;
            }

            @Override
            public Optional<R> apply(Collection<T> ts) {
                return f.apply(initial().stream().collect(Collectors.toList()));
            }
        };
    }

    protected WindowReducer(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public RingBuffer<T> initial() {
        return new ArrayRingBuffer<>(capacity);
    }
}
