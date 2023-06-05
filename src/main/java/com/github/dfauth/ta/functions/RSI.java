package com.github.dfauth.ta.functions;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;

public class RSI {

    public static final BigDecimal HUNDRED = BigDecimal.valueOf(100.000);

    public static Function<BigDecimal, Optional<BigDecimal>> rsi(int period) {
        List<BigDecimal> l = new ArrayList<>();
        return t -> {
            l.add(t);
            return calculateRSI(l, period);
        };
    }

    public static Optional<BigDecimal> calculateRSI(List<BigDecimal> prices) {
        return calculateRSI(prices, prices.size(), 3);
    }

    public static Optional<BigDecimal> calculateRSI(List<BigDecimal> prices, int period) {
        return calculateRSI(prices, period, 3);
    }

    public static Optional<BigDecimal> calculateRSI(List<BigDecimal> prices, int period, int scale) {

        if(prices.size() == 0 || prices.size() < period) {
            return Optional.empty();
        }

        List<BigDecimal> gains = new ArrayList<>();
        List<BigDecimal> losses = new ArrayList<>();
        BigDecimal period3 = BigDecimal.valueOf(period).setScale(3);
        BigDecimal periodMinus13 = BigDecimal.valueOf(period-1).setScale(3);

        List<BigDecimal> subPrices = prices.subList(0, period);

        // first pass
        for(int i=0; i<subPrices.size()-1; i++) {
            BigDecimal delta = subPrices.get(i + 1).subtract(subPrices.get(i));
            if(delta.compareTo(ZERO3) > 0) {
                gains.add(delta);
            }
            if(delta.compareTo(ZERO3) < 0) {
                losses.add(delta.abs());
            }
        }
        BigDecimal avgGain = gains.stream().reduce(BigDecimal::add).orElse(ZERO3).divide(period3, HALF_UP);
        BigDecimal avgLoss = losses.stream().reduce(BigDecimal::add).orElse(ZERO3).divide(period3, HALF_UP);
        if((avgGain.compareTo(ZERO3) == 0) ||
                avgLoss.compareTo(ZERO3) == 0) {
            return Optional.empty();
        }

        subPrices = prices.subList(prices.size() - period, prices.size());

        // second pass
        for(int i=0; i<subPrices.size()-1; i++) {
            BigDecimal delta = subPrices.get(i + 1).subtract(subPrices.get(i));
            if(delta.compareTo(ZERO3) > 0) {
                avgGain = avgGain.multiply(periodMinus13).add(delta).divide(period3,HALF_UP);
                avgLoss = avgLoss.multiply(periodMinus13).divide(period3, HALF_UP);
            }
            if(delta.compareTo(ZERO3) < 0) {
                avgGain = avgGain.multiply(periodMinus13).divide(period3,HALF_UP);
                avgLoss = avgLoss.multiply(periodMinus13).add(delta.abs()).divide(period3, HALF_UP);
            }
        }

        return Optional.of(ONE_HUNDRED.subtract(
                ONE_HUNDRED.divide(
                        ONE3.add(avgGain.divide(avgLoss, HALF_UP)),HALF_UP
                )
        ));
    }

    public static BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100).setScale(3);
    public static final BigDecimal ZERO3 = ZERO.setScale(3);
    public static final BigDecimal ONE3 = ONE.setScale(3);

    @Data
    @AllArgsConstructor
    public static class GainLoss implements Function<BigDecimal, GainLoss> {


        private final BigDecimal up;
        private final BigDecimal down;
        private final int samples;

        public GainLoss() {
            this(ZERO3, ZERO3, 0);
        }

        @Override
        public GainLoss apply(BigDecimal delta) {
            if(delta.compareTo(ZERO3) > 0) {
                return new GainLoss(up.add(delta), down, samples+1);
            } else {
                return new GainLoss(up, down.add(delta.abs()), samples+1);
            }
        }

        public GainLoss merge(GainLoss gl) {
            return new GainLoss(up.add(gl.up),down.add(gl.down), samples+gl.samples);
        }

        public Optional<BigDecimal> reduce() {
            if(up.compareTo(ZERO3) == 0 || down.compareTo(ZERO3) == 0) {
                return Optional.empty();
            } else {
                return Optional.of(ONE_HUNDRED.subtract(
                        ONE_HUNDRED.divide(
                                ONE3.add(up.divide(down, HALF_UP)),HALF_UP
                        )
                ));
            }
        }
    }
}
