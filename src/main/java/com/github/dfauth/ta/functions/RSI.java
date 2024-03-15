package com.github.dfauth.ta.functions;

import com.github.dfauth.ta.functional.Lists;
import com.github.dfauth.ta.functional.RingBufferCollector;
import com.github.dfauth.ta.model.Dated;
import com.github.dfauth.ta.util.BigDecimalOps;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import static com.github.dfauth.ta.functional.Collectors.adjacent;
import static com.github.dfauth.ta.util.BigDecimalOps.*;
import static java.math.BigDecimal.ONE;
import static java.util.function.Predicate.not;

public interface RSI {

    static BiFunction<GainLoss,GainLoss,BigDecimal> step2(int period) {
        return (prev, current) ->
            HUNDRED.subtract(BigDecimalOps.divideWithZeroCheck(HUNDRED, ONE.add(BigDecimalOps.divideWithZeroCheck(
                BigDecimalOps.multiply(prev.gain, period-1).add(current.gain),
                BigDecimalOps.multiply(prev.loss, period-1).add(current.loss)
            ).orElse(ZERO3))).orElse(ZERO3));
    }

    static BiFunction<Dated<GainLoss>, Dated<GainLoss>, Dated<BigDecimal>> datedStep2(int period) {
        return Dated.apply((d1,d2) -> step2(period).apply(d1,d2));
    }

    BinaryOperator<Dated<GainLoss>> sumDatedGainLoss = (d1,d2) -> Dated.apply(GainLoss::addStatic).apply(d1,d2);

    BiFunction<Dated<BigDecimal>,Dated<BigDecimal>,Dated<GainLoss>> toDatedGainLoss = (p,c) -> p.flatMap(_p -> c.map(_c -> GainLoss.create(_p,_c)));
    BinaryOperator<Dated<GainLoss>> add = (prev,curr) -> prev.map(curr,GainLoss::add);

    static Function<List<Dated<BigDecimal>>, Optional<Dated<BigDecimal>>> rsi() {
        return rsi(14);
    }

    static Function<List<Dated<BigDecimal>>, Optional<Dated<BigDecimal>>> rsi(int period) {

        Function<List<Dated<BigDecimal>>, List<Dated<GainLoss>>> stepOne = datedPrices -> datedPrices.stream().collect(adjacent(toDatedGainLoss));

        Function<List<Dated<GainLoss>>, Optional<Dated<BigDecimal>>> stepOnePointFive = datedGainLoss -> {
            BiFunction<Dated<GainLoss>, Dated<GainLoss>, Dated<BigDecimal>> step2 = datedStep2(period);
            List<Dated<BigDecimal>> tmp = new ArrayList<>();
            Optional<Dated<BigDecimal>> result = datedGainLoss.stream()
                    .collect(new RingBufferCollector<Dated<GainLoss>,Optional<Dated<BigDecimal>>>(
                                new Dated[period],
                                (rb,gl) -> {
                                    Optional<Dated<GainLoss>> sma = rb.stream().reduce(sumDatedGainLoss).map(dgl -> dgl.map(_gl -> _gl.divide(period)));
                                    sma.ifPresent(_sma -> tmp.add(step2.apply(_sma,gl)));
                                    rb.write(gl);
                                },
                                ignore -> Lists.last(tmp)
                            )
                    );
            return result;
        };
        return stepOne.andThen(stepOnePointFive);
    }

    static Optional<Dated<BigDecimal>> calculateRSI(List<Dated<BigDecimal>> prices, int period) {
        return rsi(period).apply(prices);
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

        public static GainLoss addStatic(GainLoss gainLoss1, GainLoss gainLoss2) {
            return new GainLoss(BigDecimalOps.add(gainLoss1.gain,gainLoss2.gain), BigDecimalOps.add(gainLoss1.loss,gainLoss2.loss));
        }

        public static GainLoss divide(GainLoss gainLoss, int period) {
            return new GainLoss(BigDecimalOps.divide(gainLoss.gain,period), BigDecimalOps.divide(gainLoss.loss,period));
        }

        public GainLoss add(GainLoss gainLoss) {
            return GainLoss.addStatic(this, gainLoss);
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
