package com.github.dfauth.ta.functional;

import com.github.dfauth.ta.model.Price;
import com.github.dfauth.ta.util.ArrayRingBuffer;
import com.github.dfauth.ta.util.RingBuffer;

import java.util.Collection;
import java.util.LinkedList;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StochasticReducer implements Reducer<Price, RingBuffer<SlowStochastic>, FastStochastic> {

    private final FastStochasticReducer nested;
    private final int slow;
    private final LinkedList<SlowStochastic> output = new LinkedList<>();

    public StochasticReducer(int fast, int slow) {
        this.slow = slow;
        nested = new FastStochasticReducer(fast, null);
    }

    @Override
    public RingBuffer<SlowStochastic> initial() {
        return new ArrayRingBuffer<>(new SlowStochastic[slow]);
    }

    @Override
    public Function<RingBuffer<SlowStochastic>, FastStochastic> finisher() {
        return b -> new FastStochastic(b.stream().collect(Collectors.toList()));
    }

    @Override
    public BiConsumer<RingBuffer<SlowStochastic>, Price> accumulator() {
        return (b,p) -> {
            
        };
    }

    private class FastStochasticReducer extends WindowingReducer<Price, Collection<SlowStochastic>, FastStochastic> {
        public FastStochasticReducer(int period, Reducer<Price, Collection<SlowStochastic>, FastStochastic> reducer) {
            super(new Price[period], reducer);
        }
    }
}
