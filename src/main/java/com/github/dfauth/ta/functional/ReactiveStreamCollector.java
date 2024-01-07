package com.github.dfauth.ta.functional;

import com.github.dfauth.ta.controller.RingBufferCollector;
import com.github.dfauth.ta.util.RingBuffer;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

@Slf4j
public class ReactiveStreamCollector<T,R> extends RingBufferCollector<T,R> implements Publisher<R>, Subscription {

    private Subscriber<? super R> subscriber;
    private AtomicLong counter;

    public ReactiveStreamCollector(T[] buffer, Function<List<T>, R> finisher) {
        super(buffer, finisher);
    }

    @Override
    public BiConsumer<RingBuffer<T>, T> accumulator() {
        return (rb,t) -> {
            super.accumulator().accept(rb,t);
            ofNullable(rb)
                    .filter(RingBuffer::isFull)
                    .filter(r -> subscriber != null)
                    .filter(r -> counter.getAndDecrement() > 0)
                    .map(finisher)
                    .ifPresent(requireNonNull(subscriber)::onNext);
        };
    }

    @Override
    public void subscribe(Subscriber<? super R> subscriber) {
        this.subscriber = subscriber;
        this.subscriber.onSubscribe(this);
    }

    @Override
    public void request(long l) {
        this.counter = new AtomicLong(l);
    }

    @Override
    public void cancel() {
        this.subscriber.onComplete();
        this.subscriber = null;
    }
}
