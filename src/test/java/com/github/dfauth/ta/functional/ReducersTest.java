package com.github.dfauth.ta.functional;

import org.junit.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.dfauth.ta.functional.Reducer.with;
import static com.github.dfauth.ta.functions.Reducers.*;
import static com.github.dfauth.ta.util.TryCatch.tryCatch;
import static com.github.dfauth.ta.util.TryCatch.tryCatchRunnable;
import static junit.framework.TestCase.assertEquals;

public class ReducersTest {

    @Test
    public void testIt() {
        {
            List<Integer> result = range(0,10).collect(with(list()));
            assertEquals(List.of(0,1,2,3,4,5,6,7,8,9), result);
        }

        {
            assertEquals(List.of(0,1,2,3,4,5,6,7,8,9), waitOn(range(0, 10).map(ReducersTest::delay).collect(future())));
        }

        {
            assertEquals(
                    Map.of(0, "0",1,"1",2,"2",3,"3",4,"4",5,"5",6,"6",7,"7",8,"8",9,"9"),
                    range(0, 10).map(i -> Map.entry(i, String.valueOf(i))).collect(groupBy())
            );
        }
    }

    public static <E> E waitForever(CompletableFuture<E> f) {
        return waitOn(f, null);
    }

    public static <E> E waitOn(CompletableFuture<E> f) {
        return waitOn(f, 10000);
    }

    public static <E> E waitOn(CompletableFuture<E> f, long millis) {
        return waitOn(f, Duration.ofMillis(millis));
    }

    public static <E> E waitOn(CompletableFuture<E> f, Duration d) {
        return Optional.ofNullable(d)
                .map(_d -> tryCatch(() -> f.get(_d.getSeconds()*1000000 + _d.getNano()/1000, TimeUnit.MICROSECONDS)))
                .orElseGet(() -> tryCatch(f::get));
    }

    public static Stream<Integer> range(int startInclusive, int endExclusive) {
        return  IntStream.range(startInclusive, endExclusive).boxed();
    }

    public static <T> CompletableFuture<T> delay(T t) {
        return delay(t, (long) (Math.random() * 1000), ForkJoinPool.commonPool());
    }

    public static <T> CompletableFuture<T> delay(T t, long delay, Executor executor) {
        CompletableFuture<T> f = new CompletableFuture<>();
        executor.execute(() -> {
            tryCatchRunnable(() -> Thread.sleep(delay));
            f.complete(t);
        });
        return f;
    }
}
