package com.github.dfauth.ta.reactive;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Processor;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Optional;
import java.util.function.Function;

@Slf4j
public class OptionalProcessor<T,R> implements Processor<Optional<T>, R> {

    private Subscriber<? super R> subscriber;
    private final Function<T,R> f;
    private Subscription subscription;

    public static <T> OptionalProcessor<T,T> identity() {
        return new OptionalProcessor<>(Function.identity());
    }

    public OptionalProcessor(Function<T, R> f) {
        this.f = f;
    }

    @Override
    public void subscribe(Subscriber<? super R> subscriber) {
        this.subscriber = subscriber;
        initialize();
    }

    private synchronized void initialize() {
        if(subscription != null) {
            if(subscriber != null) {
                subscriber.onSubscribe(subscription);
            }
        }

    }

    @Override
    public void onSubscribe(Subscription subscription) {
        this.subscription = subscription;
        initialize();
    }

    @Override
    public void onNext(Optional<T> t) {
        t.map(f).ifPresent(subscriber::onNext);
    }

    @Override
    public void onError(Throwable throwable) {
        subscriber.onError(throwable);
    }

    @Override
    public void onComplete() {
        subscriber.onComplete();
    }
}
