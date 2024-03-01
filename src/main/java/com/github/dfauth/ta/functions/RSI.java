package com.github.dfauth.ta.functions;

import com.github.dfauth.ta.functional.RingBufferPublisher;
import com.github.dfauth.ta.model.Dated;
import com.github.dfauth.ta.util.ArrayRingBuffer;
import com.github.dfauth.ta.util.BigDecimalOps;
import com.github.dfauth.ta.util.RingBuffer;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
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
import static com.github.dfauth.ta.model.Dated.dated;
import static com.github.dfauth.ta.util.BigDecimalOps.*;
import static java.math.BigDecimal.ONE;
import static java.util.function.Predicate.not;

public interface RSI {

    BiFunction<BigDecimal,BigDecimal,GainLoss> gainLoss = GainLoss::create;

    static Function<Dated<BigDecimal>, Dated<BigDecimal>> rsi() {
        return rsi(14);
    }

    static Function<Dated<BigDecimal>, Dated<BigDecimal>> rsi(int period) {
        RingBuffer<Dated<BigDecimal>> ringBuffer = new ArrayRingBuffer<Dated<BigDecimal>>(new Dated[period]);
        BiFunction<Dated<GainLoss>, Dated<GainLoss>, Dated<BigDecimal>> __f2 = (d1,d2) -> d1.flatMap(_d1 -> d2.flatMap(_d2 -> dated(d2.getLocalDate(), GainLoss.stepTwo(period).apply(_d1, _d2))));
        RingBufferPublisher<Dated<GainLoss>, Dated<BigDecimal>> gainLossPublisher = ringBufferPublisher(new Dated[period], l -> last(l.stream().collect(adjacent(__f2))));

        BiFunction<Dated<GainLoss>, Dated<GainLoss>, Dated<GainLoss>> accumulator = (previous, current) -> current.map(gl -> previous.getPayload().add(gl));
        Function<Dated<GainLoss>, Dated<BigDecimal>> combiner = d -> d.map(gl -> gl.divide(period).relativeStrengthIndex());

        return bd -> {
            ringBuffer.write(bd);
            List<Dated<GainLoss>> result = Optional.of(ringBuffer).filter(RingBuffer::isFull).flatMap(rb -> rb.collect(adjacent((prev, curr) -> curr.map(gl -> gainLoss.apply(prev.getPayload(), gl))))).orElse(Collections.emptyList());
            return result.stream().collect(collector(dated(LocalDate.now(), GainLoss.ZERO),
                    accumulator,
                    combiner
            ));
        };
    }

    static Optional<Dated<BigDecimal>> calculateRSI(List<Dated<BigDecimal>> prices) {
        return calculateRSI(prices.stream(), prices.size());
    }

    static Optional<Dated<BigDecimal>> calculateRSI(List<Dated<BigDecimal>> prices, int period) {
        return calculateRSI(prices.stream(), period);
    }

    static Optional<Dated<BigDecimal>> calculateRSI(Stream<Dated<BigDecimal>> prices, int period) {
        Function<Dated<BigDecimal>, Dated<BigDecimal>> f = rsi(period);
        return last(prices.map(f).collect(Collectors.toList()));
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
            return Optional.of(loss).filter(not(ZERO3::equals)).map(l -> BigDecimalOps.divide(gain,l)).orElse(loss);
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
