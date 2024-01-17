package com.github.dfauth.ta.functional;

import com.github.dfauth.ta.util.ArrayRingBuffer;
import com.github.dfauth.ta.util.RingBuffer;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.github.dfauth.ta.functional.Function2.peek;
import static com.github.dfauth.ta.functional.Lists.collect;

@Slf4j
public class RingBufferSubscriber<T,R> implements Subscriber<T> {

    private final RingBuffer<T> ringBuffer;
    private final Function<List<T>,R> f;
    private Subscription subscription;

    public static <T,R> RingBufferSubscriber<T,R> ringBufferSubscriber(T[] buffer, Function<List<T>, R> f) {
        return new RingBufferSubscriber<>(buffer,f);
    }

    public RingBufferSubscriber(T[] buffer, Function<List<T>, R> f) {
        this(new ArrayRingBuffer<>(buffer), f);
    }

    public RingBufferSubscriber(RingBuffer<T> ringBuffer, Function<List<T>, R> f) {
        this.ringBuffer = ringBuffer;
        this.f = f;
    }

    @Override
    public synchronized void onSubscribe(Subscription subscription) {
        this.subscription = subscription;
    }

    @Override
    public void onNext(T t) {
        Optional.of(ringBuffer)
                .map(peek(rb -> rb.write(t)))
                .filter(RingBuffer::isFull)
                .map(rb -> f.apply(collect(rb.stream())));
    }

    @Override
    public void onError(Throwable t) {
    }

    @Override
    public void onComplete() {
    }
}
