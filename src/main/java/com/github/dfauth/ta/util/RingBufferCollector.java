package com.github.dfauth.ta.util;

import com.github.dfauth.ta.functional.Lists;
import com.github.dfauth.ta.functional.MutableCollector;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

@Slf4j
public class RingBufferCollector<T,A,R> implements MutableCollector<T,RingBuffer<A>,List<R>> {
    private RingBuffer<A> ringBuffer;
    private Collector<A,?,R> collector;
    private Function<T,A> accumulatingFunction;
    private List<R> output = new ArrayList<>();

    public static <T,R> RingBufferCollector<T,T,R> of(RingBuffer<T> ringBuffer, Function<List<T>,R> f) {
        return new RingBufferCollector<>(ringBuffer, Function.identity(), f);
    }

    public RingBufferCollector(RingBuffer<A> ringBuffer, Function<T,A> accumulatingFunction, Function<List<A>,R> f) {
        this(ringBuffer, accumulatingFunction, new Collector<A,List<A>,R>() {

            @Override
            public Supplier<List<A>> supplier() {
                return ArrayList::new;
            }

            @Override
            public BiConsumer<List<A>, A> accumulator() {
                return Lists::add;
            }

            @Override
            public BinaryOperator<List<A>> combiner() {
                return Lists::add;
            }

            @Override
            public Function<List<A>, R> finisher() {
                return f;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Set.of();
            }
        });
    }

    public RingBufferCollector(RingBuffer<A> ringBuffer, Function<T,A> accumulatingFunction, Collector<A,?,R> collector) {
        this.ringBuffer = ringBuffer;
        this.accumulatingFunction = accumulatingFunction;
        this.collector = collector;
    }

    @Override
    public Supplier<RingBuffer<A>> supplier() {
        return () -> ringBuffer;
    }

    @Override
    public RingBuffer<A> initial() {
        return ringBuffer;
    }

    @Override
    public BiConsumer<RingBuffer<A>, T> accumulator() {
        return (rb, t) -> {
            rb.write(accumulatingFunction.apply(t));
            if(rb.isFull()) {
                rb.collect(collector).ifPresent(o -> output.add(o));
            }
        };
    }

    @Override
    public Function<RingBuffer<A>, List<R>> finisher() {
        return rb -> output;
    }
}
