package com.github.dfauth.util;

import com.github.dfauth.ta.util.Promise;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;

import static com.github.dfauth.ta.util.Promise.promise;
import static com.github.dfauth.ta.util.ExceptionalRunnable.tryCatchRunnable;
import static com.github.dfauth.ta.util.Promise.tryWith;
import static java.lang.Math.random;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.failedFuture;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
public class ExceptionalRunnableTest {

    @Test
    public void testIt1() {
        assertThrows(RuntimeException.class, () -> tryWith(ExceptionalRunnableTest::alwaysFail));
    }

    @Test
    public void testIt() {
        tryCatchRunnable(ExceptionalRunnableTest::sleep);
        tryCatchRunnable(ExceptionalRunnableTest::sleepAndReturn1);
        CompletableFuture<Integer> f = completedFuture(1);
        assertEquals(1, promise(f::get).recover(_ignored -> -1).get().intValue());
        CompletableFuture<Integer> f1 = failedFuture(new RuntimeException("Oops"));
        assertThrows(RuntimeException.class, () -> promise(f1::get).get());
        assertEquals(-1, promise(f1::get).recover(_ignored -> -1).get().intValue());
        Promise<Integer> p = promise(ExceptionalRunnableTest::sleepAndReturn1);
        assertTrue(p.isPending());
        assertFalse(p.isDone());
        assertEquals(1, p.get().intValue());
        assertFalse(p.isPending());
        assertTrue(p.isDone());
        CompletableFuture<Integer> f2 = completedFuture(1);
        assertEquals(1, promise(f2::get).flatMap(ExceptionalRunnableTest::sleepAndReturnPromise).get().intValue());
    }

    private static void sleep() throws InterruptedException {
        Thread.sleep(10000);
    }

    private static void alwaysFail() throws Exception {
        Thread.sleep((long)random()*1000);
        throw new Exception("Oops");
    }

    private static int sleepAndReturn1() throws InterruptedException {
        sleep();
        return 1;
    }

    private static Promise<Integer> sleepAndReturnPromise(int _ignored) {
        return promise(ExceptionalRunnableTest::sleepAndReturn1);
    }
}
