package com.github.dfauth.ta.functions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import static com.github.dfauth.ta.functional.Collectors.oops;
import static com.github.dfauth.ta.functions.Accumulator.BD_ACCUMULATOR;
import static java.math.BigDecimal.ZERO;

public class MovingAverages {

    public static Function<List<BigDecimal>, Optional<BigDecimal>> sma() {
        return l -> l.stream().reduce(BigDecimal::add).map(bd -> bd.divide(BigDecimal.valueOf(l.size()), RoundingMode.HALF_UP));
    }

    public static Function<BigDecimal, Optional<BigDecimal>> sma(int period) {
        return sma(period, BD_ACCUMULATOR.get());
    }

    public static <T> Function<List<T>, Optional<T>> sma(BinaryOperator<T> addition, BiFunction<T,Double,T> division) {
        return l -> l.stream().reduce(addition).map(u -> division.apply(u, (double) l.size()));
    };

    public static <T> Function<List<T>, Optional<T>> ema(BinaryOperator<T> addition, BiFunction<T,Double,T> multiplication, BiFunction<T,Double,T> division) {
        return ema(2, addition, multiplication, division);
    }

    public static <T> Function<List<T>, Optional<T>> ema(int smoothingFactor, BinaryOperator<T> addition, BiFunction<T,Double,T> multiplication, BiFunction<T,Double,T> division) {
        return l -> {
            Optional<T> firstValue = sma(addition, division).apply(l);
            double weight = (double) smoothingFactor / (1 + l.size());
            return firstValue.map(fv -> l.stream()
                    .reduce(fv,
                            (ema, v) -> addition.apply(multiplication.apply(v, weight), multiplication.apply(ema, 1.0d - weight)),
                            oops()
                    ));
        };
    };

    public static <T extends Number, R extends Number> Function<T, Optional<R>> sma(int period, Accumulator<T,R> ops) {
        LinkedList<T> l = new LinkedList<>();
        return t -> {
            l.add(t);
            ops.add(t);
            if(l.size() > period) {
                ops.subtract(l.remove(0));
                return Optional.of(ops.divide(l.size()));
            } else {
                return Optional.empty();
            }
        };
    }

    public static Function<BigDecimal, Optional<BigDecimal>> ema(int period) {
        if (period <= 0) {
            throw new IllegalArgumentException("Period should be a positive integer");
        }
        BigDecimal alpha = BigDecimal.valueOf(2.0 / (period + 1));
        var ref = new Object() {
            BigDecimal ema = ZERO;
            int count = 0;
        };
        return t -> {
                if (ref.count < period) {
                    ref.ema = ref.ema.add(t);
                    ref.count++;
                    if (ref.count == period) {
                        ref.ema = ref.ema.divide(BigDecimal.valueOf(period), RoundingMode.HALF_UP);
                        return Optional.of(ref.ema);
                    } else {
                        return Optional.empty();
                    }
                } else {
                    ref.ema = alpha.multiply(t.subtract(ref.ema)).add(ref.ema);
                    return Optional.of(ref.ema);
                }
        };
    }

    public static BigDecimal calculateSMA(List<BigDecimal> prices) {
        return calculateSMA(prices, prices.size());
    }

    public static BigDecimal calculateSMA(List<BigDecimal> prices, int period) {
        List<BigDecimal> subList = prices.subList(prices.size() - period, prices.size());
        return subList.stream().reduce(ZERO, BigDecimal::add).divide(BigDecimal.valueOf(period),RoundingMode.HALF_UP);
    }
    public static BigDecimal calculateEMA(List<BigDecimal> prices) {
        return calculateEMA(prices, prices.size());
    }

    public static BigDecimal calculateEMA(List<BigDecimal> prices, int period) {

        // Calculate the smoothing factor.
        BigDecimal smoothingFactor = BigDecimal.valueOf(2.0d / (period + 1));

        // Calculate the first EMA.
        BigDecimal firstEMA = prices.stream().limit(period).map(average()).findFirst().orElse(ZERO);

        // Calculate the next EMA.
        for (int i = period; i < prices.size(); i++) {
            BigDecimal currentEMA = firstEMA.add(smoothingFactor.multiply(prices.get(i).subtract(firstEMA)));
            firstEMA = currentEMA;
        }

        return firstEMA;
    }

    public static Function<BigDecimal,BigDecimal> average() {
        return average(2);
    }

    public static Function<BigDecimal,BigDecimal> average(int scale) {
        var ref = new Object() {
            BigDecimal sum = ZERO.setScale(scale);
            int cnt = 0;
        };
            return bd -> {
                ref.sum = ref.sum.add(bd);
                ref.cnt++;
                return ref.sum.divide(BigDecimal.valueOf(ref.cnt).setScale(scale), RoundingMode.HALF_UP);
            };
    }
}
