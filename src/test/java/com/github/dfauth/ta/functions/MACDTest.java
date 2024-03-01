package com.github.dfauth.ta.functions;

import com.github.dfauth.ta.functional.MACD;
import com.github.dfauth.ta.functional.Tuple2;
import com.github.dfauth.ta.model.PriceAction;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static com.github.dfauth.ta.functional.Lists.mapList;
import static com.github.dfauth.ta.functional.Lists.splitAt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class MACDTest {

    @Test
    public void testMySanity() {
        {
            Flux<Integer> shared = Flux.just(1, 2, 3).share();
            List<Integer> a = new ArrayList<>();
            List<Integer> b = new ArrayList<>();
            shared.subscribe(a::add);
            shared.subscribe(b::add);
            assertEquals(List.of(1,2,3),a);
            assertEquals(List.of(1,2,3),b);
            assertEquals(a,b);
            assertTrue(a != b);
        }

        {
            AtomicBoolean ab = new AtomicBoolean(false);
            Stream<Integer> stream = List.of(1,2,3).stream();
            Flux<Integer> shared = Flux.from(_wrap(stream)).share();
            List<Integer> a = new ArrayList<>();
            List<Integer> b = new ArrayList<>();
            shared.filter(s -> ab.get()).subscribe(a::add);
            shared.filter(s -> ab.get()).subscribe(b::add);
            ab.set(true);
            assertEquals(List.of(1,2,3),a);
            assertEquals(List.of(1,2,3),b);
            assertEquals(a,b);
            assertTrue(a != b);
        }
    }

    private <T> Publisher<T> _wrap(Stream<T> stream) {

        return new Publisher<>(){
            @Override
            public void subscribe(Subscriber<? super T> subscriber) {
                subscriber.onSubscribe(new AtomicSubscription(2, stream, subscriber));
            }
        };
    }

    private class AtomicSubscription<T> implements Subscription {

        private final Stream<T> stream;
        private final Subscriber<T> subscriber;
        private AtomicLong latch;

        public AtomicSubscription(int cnt, Stream<T> stream, Subscriber<T> subscriber) {
            this.latch = new AtomicLong(cnt);
            this.stream = stream;
            this.subscriber = subscriber;
        }

        @Override
        public void request(long l) {
            if(this.latch.decrementAndGet() <= 0) {
                this.stream.forEach(subscriber::onNext);
            }
        }

        @Override
        public void cancel() {
            this.latch.set(0);
        }
    }

    @Test
    public void testWGX() {
        int period = 26;
        Tuple2<List<PriceAction>, List<PriceAction>> subList = splitAt(mapList(TestData.WGX, PriceAction.class::cast), TestData.WGX.size() - 2*period);
        MACD macd = MACD.calculateMACD(subList._2().stream()).get();
        assertEquals(
                -0.032d,
                macd.getMacd().doubleValue(),
                0.001d
        );
        assertEquals(
                0.003d,
                macd.getSignal().doubleValue(),
                0.001d
        );
    }

}
