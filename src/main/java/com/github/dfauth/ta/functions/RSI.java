package com.github.dfauth.ta.functions;

import com.github.dfauth.ta.functional.RingBufferPublisher;
import com.github.dfauth.ta.util.ArrayRingBuffer;
import com.github.dfauth.ta.util.BigDecimalOps;
import com.github.dfauth.ta.util.RingBuffer;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.dfauth.ta.functional.Collectors.adjacent;
import static com.github.dfauth.ta.functional.ImmutableCollector.collector;
import static com.github.dfauth.ta.functional.Lists.last;
import static com.github.dfauth.ta.functional.RingBufferPublisher.ringBufferPublisher;
import static com.github.dfauth.ta.util.BigDecimalOps.*;
import static java.math.BigDecimal.ONE;

public interface RSI {

    BiFunction<BigDecimal,BigDecimal,GainLoss> gainLoss = GainLoss::create;

    static Function<BigDecimal, Optional<BigDecimal>> rsi() {
        return rsi(14);
    }

    static Function<BigDecimal, Optional<BigDecimal>> rsi(int period) {
        RingBuffer<BigDecimal> ringBuffer = new ArrayRingBuffer<>(new BigDecimal[period]);
        RingBufferPublisher<GainLoss, BigDecimal> gainLossPublisher = ringBufferPublisher(new GainLoss[period], l -> last(l.stream().collect(adjacent(GainLoss.stepTwo(period)))));

        return bd -> {
            ringBuffer.write(bd);
            Optional<List<GainLoss>> result = Optional.of(ringBuffer).filter(RingBuffer::isFull).flatMap(rb -> rb.collect(adjacent(gainLoss)));
            return result.map(r -> r.stream().collect(collector(GainLoss.ZERO,
                    GainLoss::add,
                    gl -> gl.divide(period).relativeStrengthIndex()))
            );
        };
    }

    static Optional<BigDecimal> calculateRSI(List<BigDecimal> prices) {
        return calculateRSI(prices.stream(), prices.size());
    }

    static Optional<BigDecimal> calculateRSI(List<BigDecimal> prices, int period) {
        return calculateRSI(prices.stream(), period);
    }

    static Optional<BigDecimal> calculateRSI(Stream<BigDecimal> prices, int period) {
        Function<BigDecimal, Optional<BigDecimal>> f = rsi(period);
        return last(prices.map(f).flatMap(Optional::stream).collect(Collectors.toList()));
    }

    @Data
    @AllArgsConstructor
    class GainLoss {

        private final BigDecimal gain;
        private final BigDecimal loss;

        static GainLoss ZERO = new GainLoss(ZERO3,ZERO3);

        public static GainLoss create(BigDecimal previous, BigDecimal current) {
            BigDecimal diff = valueOf(current.subtract(previous));
            return isGreaterThanOrEqualTo(diff,ZERO3) ? new GainLoss(diff, ZERO3) : new GainLoss(ZERO3, diff.abs());
        }

        public static GainLoss add1(GainLoss gainLoss1, GainLoss gainLoss2) {
            return new GainLoss(BigDecimalOps.add(gainLoss1.gain,gainLoss2.gain), BigDecimalOps.add(gainLoss1.loss,gainLoss2.loss));
        }

        public static GainLoss divide(GainLoss gainLoss, int period) {
            return new GainLoss(BigDecimalOps.divide(gainLoss.gain,period), BigDecimalOps.divide(gainLoss.loss,period));
        }

        public GainLoss add(GainLoss gainLoss) {
            return add1(this, gainLoss);
        }

        public GainLoss divide(int period) {
            return divide(this, period);
        }

        public BigDecimal relativeStrength() {
            return BigDecimalOps.divide(gain,loss);
        }

        public BigDecimal relativeStrengthIndex() {
            return subtract(HUNDRED, BigDecimalOps.divide(HUNDRED, ONE.add(relativeStrength())));
        }

        public static BiFunction<GainLoss,GainLoss,BigDecimal> stepTwo(int period) {
            return (prev,curr) -> curr.stepTwo(prev, period);
        }

        public BigDecimal stepTwo(GainLoss previousAvg, int period) {
            return HUNDRED.subtract(BigDecimalOps.divide(HUNDRED,ONE3.add(smooth(previousAvg, period))));
        }

        private BigDecimal smooth(GainLoss previousAvg, int period) {
            return BigDecimalOps.divide(
                    multiply(previousAvg.getGain(),period-1).add(gain),
                    multiply(previousAvg.getLoss(),period-1).add(loss)
            );
        }
    }
}
