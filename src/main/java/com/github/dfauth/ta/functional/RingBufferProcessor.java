package com.github.dfauth.ta.functional;

import com.github.dfauth.ta.util.ArrayRingBuffer;
import com.github.dfauth.ta.util.RingBuffer;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Processor;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.github.dfauth.ta.functional.Function2.peek;
import static com.github.dfauth.ta.functional.Lists.collect;

@Slf4j
public class RingBufferProcessor<T,R> implements Processor<T,R> {

    private final RingBuffer<T> ringBuffer;
    private final Function<List<T>,R> f;
    private Subscriber<? super R> subscriber;
    private Subscription subscription;

    public static <T,R> RingBufferProcessor<T,R> ringBufferProcessor(T[] buffer, Function<List<T>, R> f) {
        return new RingBufferProcessor<>(buffer,f);
    }

    public RingBufferProcessor(T[] buffer, Function<List<T>, R> f) {
        this(new ArrayRingBuffer<>(buffer), f);
    }

    public RingBufferProcessor(RingBuffer<T> ringBuffer, Function<List<T>, R> f) {
        this.ringBuffer = ringBuffer;
        this.f = f;
    }

    @Override
    public synchronized void subscribe(Subscriber<? super R> subscriber) {
        this.subscriber = subscriber;
        tryInit();
    }

    private void tryInit() {
        Optional.ofNullable(this.subscriber).ifPresent(s1 -> Optional.ofNullable(this.subscription).ifPresent(s1::onSubscribe));
    }

    @Override
    public synchronized void onSubscribe(Subscription subscription) {
        this.subscription = subscription;
        tryInit();
    }

    @Override
    public void onNext(T t) {
        Optional.ofNullable(subscriber).ifPresent(s -> {
            Optional.of(ringBuffer)
                    .map(peek(rb -> rb.write(t)))
                    .filter(RingBuffer::isFull)
                    .map(rb -> f.apply(collect(rb.stream())))
                    .ifPresent(s::onNext);
        });
    }

    @Override
    public void onError(Throwable t) {
        Optional.ofNullable(subscriber).ifPresent(s -> s.onError(t));
    }

    @Override
    public void onComplete() {
        Optional.ofNullable(subscriber).ifPresent(Subscriber::onComplete);
    }
}
