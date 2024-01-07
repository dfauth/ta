package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.functional.MutableCollector;
import com.github.dfauth.ta.util.ArrayRingBuffer;
import com.github.dfauth.ta.util.RingBuffer;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;

@Slf4j
public class RingBufferCollector<T,R> extends MutableCollector<T,RingBuffer<T>,R> {

    public RingBufferCollector(T[] buffer, Function<List<T>, R> finisher) {
        super(new ArrayRingBuffer<>(buffer),
                (_rb,t) -> {
                    _rb.write(t);
                    return _rb;
                },
                _rb -> finisher.apply(_rb.stream().collect(Collectors.toList())));
    }

    public static <T> Collector<T, RingBuffer<T>, List<T>> ringBufferCollector(T[] buffer) {
        return ringBufferCollector(buffer, identity());
    }

    public static <T,R> Collector<T, RingBuffer<T>,R> ringBufferCollector(T[] buffer, Function<List<T>,R> finisher) {
        return new RingBufferCollector<>(buffer, finisher);
    }

}
