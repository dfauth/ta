package com.github.dfauth.ta.functional;

import com.github.dfauth.ta.util.ArrayRingBuffer;
import com.github.dfauth.ta.util.RingBuffer;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import static com.github.dfauth.ta.functional.Function2.peek;
import static com.github.dfauth.ta.functional.Lists.collect;

@Slf4j
public class RingBufferPublisher<T,R> implements Publisher<R>, Subscription {

    private final RingBuffer<T> ringBuffer;
    private final Function<List<T>,Optional<R>> f;
    private Subscriber<? super R> subscriber;
    private AtomicLong counter;

    public static <T,R> RingBufferPublisher<T,R> ringBufferPublisher(T[] buffer, Function<List<T>, Optional<R>> f) {
        return new RingBufferPublisher<>(buffer,f);
    }

    public RingBufferPublisher(T[] buffer, Function<List<T>, Optional<R>> f) {
        this.ringBuffer = new ArrayRingBuffer<>(buffer);
        this.f = f;
    }

    @Override
    public synchronized void subscribe(Subscriber<? super R> subscriber) {
        this.subscriber = subscriber;
        subscriber.onSubscribe(this);
    }

    @Override
    public void request(long l) {
        this.counter = new AtomicLong(l);
    }

    @Override
    public void cancel() {
        Optional.ofNullable(subscriber).ifPresent(s -> {
            s.onComplete();
            subscriber = null;
            this.counter = new AtomicLong(0);
        });
    }

    public void write(T t) {
        Optional.of(ringBuffer)
                .map(peek(rb -> rb.write(t)))
                .filter(RingBuffer::isFull)
                .flatMap(rb -> f.apply(collect(rb.stream())))
                .ifPresent(r -> Optional.ofNullable(subscriber)
                        .filter(s -> this.counter.getAndDecrement() > 0)
                        .ifPresent(s ->s.onNext(r))
                );

    }

    public Function<T,Publisher<R>> asFunction() {
        return t -> {
            write(t);
            return this;
        };
    }

}
