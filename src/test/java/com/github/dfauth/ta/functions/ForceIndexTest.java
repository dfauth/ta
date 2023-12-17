package com.github.dfauth.ta.functions;

import com.github.dfauth.ta.functional.ImmutableCollector;
import com.github.dfauth.ta.functional.Lists;
import com.github.dfauth.ta.util.ArrayRingBuffer;
import com.github.dfauth.ta.util.RingBuffer;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

@Slf4j
public class ForceIndexTest {

    private static final List<Integer> TEST_DATA = List.of(2,4,8);

    @Test
    public void testIt() {

        BiFunction<Integer, Integer, Optional<BigDecimal>> accumulator = (prev, curr) -> Optional.ofNullable(prev).map(p -> BigDecimal.valueOf(curr).subtract(BigDecimal.valueOf(p)));
        RingBuffer<Integer> ringBuffer = new ArrayRingBuffer<>(new Integer[2]);
        Function<List<Integer>,BigDecimal> f = l -> BigDecimal.valueOf(l.stream().mapToInt(Integer::intValue).sum());
        List<BigDecimal> result = TEST_DATA.stream().collect(new Blah(accumulator, null)); //RingBufferCollector.of(ringBuffer, f)));
        assertEquals(List.of(3.0d), result.stream().map(BigDecimal::doubleValue).collect(Collectors.toList()));

    }

    public static class Blah implements ImmutableCollector<Integer, List<Intermediate<Integer,Optional<BigDecimal>>>, List<BigDecimal>> {

        private BiFunction<Integer, Integer, Optional<BigDecimal>> accumulator;
        private Collector<BigDecimal, ? extends Object, List<BigDecimal>> collector;

        public Blah(BiFunction<Integer, Integer, Optional<BigDecimal>> accumulator, Collector<BigDecimal, ? extends Object, List<BigDecimal>> collector) {
            this.accumulator = accumulator;
            this.collector = collector;
        }

        @Override
        public List<Intermediate<Integer,Optional<BigDecimal>>> initial() {
            return new ArrayList<>();
        }

        @Override
        public BiFunction<List<Intermediate<Integer,Optional<BigDecimal>>>, Integer, List<Intermediate<Integer,Optional<BigDecimal>>>> accumulatingFunction() {
            return (l,i) -> Lists.last(l)
                    .map(Intermediate::previousInput)
                    .flatMap(p -> accumulator.apply(p, i))
                    .map(o -> Lists.add(l,Intermediate.of(i, Optional.of(o))))
                    .orElseGet(() -> List.of(Intermediate.of(i,Optional.empty())));
        }

        @Override
        public Function<List<Intermediate<Integer,Optional<BigDecimal>>>, List<BigDecimal>> finishingFunction() {
            return intermediates -> intermediates.stream().map(Intermediate::previousOutput).flatMap(Optional::stream).collect(collector);
        }
    }

    public interface Intermediate<T,A> {
        static <T,A> Intermediate<T,A> of(T t, A a) {
            return new Intermediate<T, A>() {
                @Override
                public T previousInput() {
                    return t;
                }

                @Override
                public A previousOutput() {
                    return a;
                }
            };
        }
        T previousInput();
        A previousOutput();
    }

}
