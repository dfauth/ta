package com.github.dfauth.ta.functional;

import com.github.dfauth.ta.model.PriceAction;
import lombok.AllArgsConstructor;
import lombok.Data;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static com.github.dfauth.ta.functional.Collectors.EMA;
import static com.github.dfauth.ta.functional.Collectors.zipWithMostRecent;
import static com.github.dfauth.ta.functional.Lists.collect;
import static com.github.dfauth.ta.functional.RingBufferPublisher.ringBufferPublisher;
import static com.github.dfauth.ta.util.ExceptionalRunnable.tryCatch;
import static com.github.dfauth.ta.util.OptionalOps.toIterable;

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
        List<PriceAction> list = collect(prices);

        CompletableFuture<MACD> fut = new CompletableFuture<>();

        RingBufferPublisher<BigDecimal, BigDecimal> slowEmaPublisher = ringBufferPublisher(new BigDecimal[slow], EMA);
        RingBufferPublisher<BigDecimal, BigDecimal> fastEmaPublisher = ringBufferPublisher(new BigDecimal[fast], EMA);
        RingBufferPublisher<BigDecimal, Tuple2<Optional<BigDecimal>,BigDecimal>> signalPublisher = ringBufferPublisher(new BigDecimal[signal], zipWithMostRecent(EMA));

        Flux<BigDecimal> slowThingy = Flux.fromStream(list.stream().map(PriceAction::getClose)).flatMap(slowEmaPublisher.asFunction());
        Flux<BigDecimal> fastThingy = Flux.fromStream(list.stream().map(PriceAction::getClose)).flatMap(fastEmaPublisher.asFunction());
        Flux<MACD> macdFlux = slowThingy.zipWith(fastThingy, BigDecimal::subtract).log().flatMap(signalPublisher.asFunction())
                .flatMapIterable(t2 -> toIterable(t2._1().map(s -> new MACD(t2._2(), s))));
        macdFlux.log().subscribe(fut::complete);

        return tryCatch(() -> Optional.of(fut.get(10000, TimeUnit.MILLISECONDS)), ignore -> Optional.empty());
    }

}
