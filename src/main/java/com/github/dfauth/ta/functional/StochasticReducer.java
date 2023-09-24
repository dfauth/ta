package com.github.dfauth.ta.functional;

import com.github.dfauth.ta.model.Price;
import com.github.dfauth.ta.util.ArrayRingBuffer;
import com.github.dfauth.ta.util.RingBuffer;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StochasticReducer implements Reducer<Price, RingBuffer<Price>, FastStochastic> {

    private int period;
    private RingBuffer<SlowStochastic> buffer;

    public StochasticReducer(int fast, int slow) {
        this.period = slow;
        this.buffer = new ArrayRingBuffer<>(fast);
    }

    @Override
    public RingBuffer<Price> initial() {
        return new ArrayRingBuffer<>(period);
    }

    @Override
    public Function<RingBuffer<Price>, FastStochastic> finisher() {
        return b -> new FastStochastic(buffer.stream().collect(Collectors.toList()));
    }

    @Override
    public BiConsumer<RingBuffer<Price>, Price> accumulator() {
        return (b,p) -> {
            b.add(p);
            if(b.isFull()) {
                buffer.add(b.stream().reduce(new SlowStochastic(), SlowStochastic::merge, SlowStochastic::merge));
            }
        };
    }
}
