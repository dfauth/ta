package com.github.dfauth.ta.functional;

import com.github.dfauth.ta.util.ArrayRingBuffer;
import com.github.dfauth.ta.util.RingBuffer;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Slf4j
public class WindowingReducer<T,R,S> implements Reducer<T, RingBuffer<T>, List<S>> {

    private T[] buffer;
    private Reducer<T, R, S> reducer;
    private LinkedList<S> output = new LinkedList<>();

    public WindowingReducer(T[] buffer, Reducer<T, R, S> reducer) {
        this.buffer = buffer;
        this.reducer = reducer;
    }

    @Override
    public RingBuffer<T> initial() {
        return new ArrayRingBuffer<>(buffer);
    }

    @Override
    public Function<RingBuffer<T>, List<S>> finisher() {
        return ignored -> output;
    }

    @Override
    public BiConsumer<RingBuffer<T>, T> accumulator() {
        return (b,t) -> {
            b.write(t);
            if(b.isFull()) {
                output.add(b.stream().collect(reducer));
            }
        };
    }
}
