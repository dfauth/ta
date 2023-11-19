package com.github.dfauth.ta.util;

import com.github.dfauth.ta.functional.PriceActionFunction;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

public interface CalculatingRingBuffer<T,A,R> extends RingBuffer<T>, PriceActionFunction<T,A,R> {

    default Optional<R> calculate() {
        return calculate(this);
    }

    static <T,A,R> CalculatingRingBuffer<T,A,R> create(RingBuffer<T> ringBuffer, String name, PriceActionFunction<T,A,R> paf) {
        return new CalculatingRingBuffer<>(){
            @Override
            public String name() {
                return String.format("%s(%d)",name, ringBuffer.capacity());
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

            @Override
            public A initial() {
                return paf.initial();
            }

            @Override
            public BiConsumer<AtomicReference<A>, T> accumulator() {
                return paf.accumulator();
            }

            @Override
            public Function<AtomicReference<A>, R> finisher() {
                return paf.finisher();
            }

        };
    }

    String name();
}
