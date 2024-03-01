package com.github.dfauth.ta.functional;

import com.github.dfauth.ta.model.PriceAction;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.github.dfauth.ta.functional.Collectors.EMA;
import static com.github.dfauth.ta.functional.Collectors.zipWithMostRecent;
import static com.github.dfauth.ta.functional.RingBufferPublisher.ringBufferPublisher;
import static com.github.dfauth.ta.util.OptionalOps.toIterable;
import static io.github.dfauth.trycatch.ExceptionalRunnable.tryCatch;

@Data
@AllArgsConstructor
public class MACD {

    final BigDecimal macd;
    final BigDecimal signal;

    public BigDecimal histogram() {
        return macd.subtract(signal);
    }

    public static Optional<MACD> calculateMACD(Stream<PriceAction> prices) {
        return calculateMACD(prices, 12,26,9);
    }

    public static Optional<MACD> calculateMACD(Stream<PriceAction> prices, int fast, int slow, int signal) {

        CompletableFuture<MACD> fut = new CompletableFuture<>();

        Flux<PriceAction> sharedSource = Flux.from(adaptStream(prices)).share();

        RingBufferPublisher<BigDecimal, BigDecimal> slowEmaPublisher = ringBufferPublisher(new BigDecimal[slow], EMA);
        RingBufferPublisher<BigDecimal, BigDecimal> fastEmaPublisher = ringBufferPublisher(new BigDecimal[fast], EMA);
        RingBufferPublisher<BigDecimal, Tuple2<Optional<BigDecimal>,BigDecimal>> signalPublisher = ringBufferPublisher(new BigDecimal[signal], zipWithMostRecent(EMA));

        Flux<BigDecimal> fastThingy = sharedSource.map(PriceAction::getClose).flatMap(fastEmaPublisher.asFunction());
        Flux<BigDecimal> slowThingy = sharedSource.map(PriceAction::getClose).flatMap(slowEmaPublisher.asFunction());
        Flux<MACD> macdFlux = slowThingy.zipWith(fastThingy, BigDecimal::subtract).log().flatMap(signalPublisher.asFunction())
                .flatMapIterable(t2 -> toIterable(t2._1().map(s -> new MACD(t2._2(), s))));
        macdFlux.log().subscribe(fut::complete);

        return tryCatch(() -> Optional.of(fut.get(10000, TimeUnit.MILLISECONDS)), ignore -> Optional.empty());
    }

    private static <T> Publisher<T> adaptStream(Stream<T> stream) {
        return subscriber -> subscriber.onSubscribe(new AtomicSubscription<>(stream, subscriber::onNext));
    }

    private static class AtomicSubscription<T> implements Subscription {

        private final Stream<T> stream;
        private final Consumer<T> consumer;
        private AtomicLong latch;

        public AtomicSubscription(Stream<T> stream, Consumer<T> consumer) {
            this.stream = stream;
            this.consumer = consumer;
        }

        @Override
        public void request(long l) {
            Optional.ofNullable(latch).ifPresentOrElse(_l -> {}, () -> {
                latch = new AtomicLong(l);
                stream.forEach(consumer);
            });
        }

        @Override
        public void cancel() {
            request(0);
        }

        public boolean decrement(T t) {
            return latch.getAndDecrement() > 0;
        }
    }

}
