package com.github.dfauth.ta.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dfauth.ta.functional.Lists;
import com.github.dfauth.ta.functional.Maps;
import com.github.dfauth.ta.functions.CAGR;
import com.github.dfauth.ta.util.BigDecimalOps;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;
import java.util.stream.Collector;

import static com.github.dfauth.ta.functional.Optionals.*;
import static com.github.dfauth.ta.model.Dated.dated;
import static java.lang.Math.abs;
import static java.math.BigDecimal.ZERO;
import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Getter
public class TradingMetrics {

    public static final Function<Double, Function<Double, Function<Double, Double>>> expectancy = winRate -> avgGain -> avgLoss -> (winRate * avgGain) - ((1.0d - winRate) * abs(avgLoss));
    public static final BiFunction<Double, Double, Double> riskRewardRatio = (avgGain, avgLoss) -> abs(avgGain / avgLoss);
    public static final BiFunction<Double, Double, Double> roi = (profit, investment) -> profit / investment;
    public static final BiFunction<Double, Double, Double> cagr = CAGR::cagr;
    public static final Function<Double, Function<Double, Function<Double,Double>>> positiveExpectancy = avgWin -> avgLoss -> winRate -> (1 + (avgWin/abs(avgLoss))) * winRate - 1.0d;

    private int losingPositions;
    private int winningPositions;
    private BigDecimal weightedHoldingTime;
    private int unitsPurchased;
    private BigDecimal purchaseValue;
    private BigDecimal saleValue;
    private BigDecimal mktValue;
    private BigDecimal totalLoss;
    private BigDecimal totalGain;
    private LocalDate start;
    private LocalDate end;
    private long duration;
    private int positions;
    private int openPositions;
    private BigDecimal dividends;
    @JsonIgnore
    private TreeMap<LocalDate, List<Trade>> trades = new TreeMap<>(LocalDate::compareTo);

    @JsonGetter("mv")
    public Optional<BigDecimal> getMarketValue() {
        return ofNullable(mktValue);
    }

    @JsonGetter("trades")
    public long getTrades() {
        return trades.values().stream().flatMap(List::stream).count();
    }

    @JsonGetter("maxInvestment")
    public MaxInvestment getMaxInvestment() {
        return trades.entrySet().stream()
                .map(e -> dated(e.getKey(), e.getValue().stream()
                        .map(Trade::getValue)
                        .reduce(BigDecimal::add).orElse(ZERO)))
                .reduce(new MaxInvestment(),
                        MaxInvestment::apply,
                        MaxInvestment::merge);
    }

    public Optional<BigDecimal> getAverageLoss() {
        return bothPresent(getTotalLoss(), getLosingPositions(), BigDecimalOps::divide);
    }

    public Optional<BigDecimal> getAverageGain() {
        return bothPresent(getTotalGain(), getWinningPositions(), BigDecimalOps::divide);
    }

    public double getWinRate() {
        return (double) getWinningPositions() / getPositions();
    }

    public Optional<Double> getExpectancy() {
        return allPresent(expectancy, Optional.of(getWinRate()), getAverageGain().map(BigDecimal::doubleValue), getAverageLoss().map(BigDecimal::doubleValue));
    }

    public Optional<Double> getRiskRewardRatio() {
        return bothPresent(getAverageGain().map(BigDecimal::doubleValue), getAverageLoss().map(BigDecimal::doubleValue), riskRewardRatio);
    }

    public Optional<Double> getPositiveExpectancy() {
        return allPresent(positiveExpectancy,
                getAverageGain().map(BigDecimal::doubleValue),
                getAverageLoss().map(BigDecimal::doubleValue).map(Math::abs),
                Optional.of(getWinRate()));
    }

    public Optional<BigDecimal> getAveragePositionSize() {
        return bothPresent(getPurchaseValue(), getPositions(), BigDecimalOps::divide);
    }

    public Optional<BigDecimal> getAverageTradeSize() {
        return getTurnover().map(t -> BigDecimalOps.divide(t, getTrades()));
    }

    public Optional<Double> getRoi() {
        return getProfit().map(BigDecimal::doubleValue).map(p -> p/getMaxInvestment().getMaxValue().getPayload().doubleValue());
    }

    public Optional<BigDecimal> getProfit() {
        return bothPresent(getTotalGain(),getTotalLoss(), BigDecimal::add);
    }

    public Optional<BigDecimal> getReturn() {
        return getProfit().map(p -> p.divide(getMaxInvestment().getMaxValue().getPayload(),RoundingMode.HALF_UP));
    }

    public Optional<BigDecimal> getTurnover() {
        return eitherOrBoth(getPurchaseValue(), getSaleValue(), (pv,sv) -> pv.abs().add(sv));
    }

    public int getClosedPositions() {
        return positions - openPositions;
    }

    public Optional<Double> getOccupancy() {
        return bothPresent(getStart(), getEnd(), (s,e) -> Duration.between(s.atStartOfDay(), e.atStartOfDay()).toDays())
                .map(elapsed -> (double) getDuration() / (getPositions() * elapsed));
    }

    public Optional<Double> getCagr() {
        Optional<Double> periods = ofNullable(getWeightedHoldingTime()).map(BigDecimal::doubleValue).map(d -> d / (365.25 * getPositions()));
        return getReturn().map(BigDecimal::doubleValue).flatMap(r -> periods.map(p -> CAGR.cagr(r,p)));
    }

    public TradingMetrics add(TradingMetrics other) {
        return new TradingMetrics(
                losingPositions + other.losingPositions,
                winningPositions + other.winningPositions,
                weightedHoldingTime.add(other.weightedHoldingTime),
                unitsPurchased + other.unitsPurchased,
                purchaseValue.add(other.purchaseValue),
                saleValue.add(other.saleValue),
                eitherOrBoth(getMarketValue(), other.getMarketValue(), (BigDecimal l, BigDecimal r) -> l.add(r)).orElse(null),
                totalLoss.add(other.totalLoss),
                totalGain.add(other.totalGain),
                start.isBefore(other.start) ? start : other.start,
                end.isAfter(other.end) ? end : other.end,
                duration + other.duration,
                positions + other.positions,
                openPositions + other.openPositions,
                dividends.add(other.dividends),
                Maps.merge(() -> new TreeMap<>(LocalDate::compareTo), Lists::add, trades, other.trades)
        );
    }

    public TradingMetrics add(Position p) {

        return new TradingMetrics(
                p.isProfitable() ? losingPositions : losingPositions + 1,
                p.isProfitable() ? winningPositions+1 : winningPositions,
                eitherOrBoth(weightedHoldingTime, p.getWeightedHoldingTime(), BigDecimal::add).orElse(null),
                unitsPurchased + p.getUnitsPurchased(),
                eitherOrBoth(purchaseValue, p.getPurchaseValue(), BigDecimal::add).orElse(null),
                eitherOrBoth(saleValue, p.getSaleValue(), BigDecimal::add).orElse(null),
                eitherOrBoth(getMarketValue(), p.getMarketValue(), (BigDecimal l, BigDecimal r) -> l.add(r)).orElse(null),
                p.getProfit().filter(BigDecimalOps::isLessThanZero).map(_p -> Optional.ofNullable(totalLoss).map(tl -> tl.add(_p)).orElse(_p)).orElse(totalLoss),
                p.getProfit().filter(BigDecimalOps::isGreaterThanZero).map(_p -> Optional.ofNullable(totalGain).map(tl -> tl.add(_p.abs())).orElse(_p)).orElse(totalGain),
                eitherOrBoth(start, p.getOpen(), (s, d) -> s.isBefore(d) ? s : d).orElse(null),
                eitherOrBoth(end, p.getLast(), (s, d) -> s.isAfter(d) ? s : d).orElse(null),
                duration + p.getDuration(),
                positions + 1,
                p.isOpen() ? openPositions + 1 : openPositions,
                p.getDividends().map(_p -> ofNullable(dividends).map(_p::add).orElse(_p)).orElse(dividends),
                Maps.merge(() -> new TreeMap<>(LocalDate::compareTo), Lists::add,trades, p.getTrades().stream().collect(groupingBy(t -> t.getDate().toLocalDateTime().toLocalDate())))
        );
    }

    public static Collector<Position, ?, TradingMetrics> collector() {
        return new Collector<Position, AtomicReference<TradingMetrics>, TradingMetrics>() {
            @Override
            public Supplier<AtomicReference<TradingMetrics>> supplier() {
                return () -> new AtomicReference<>(new TradingMetrics());
            }

            @Override
            public BiConsumer<AtomicReference<TradingMetrics>, Position> accumulator() {
                return (ref, p) -> ref.getAndUpdate(tm -> tm.add(p));
            }

            @Override
            public BinaryOperator<AtomicReference<TradingMetrics>> combiner() {
                return (l , r) -> {
                    l.getAndUpdate(tm -> tm.add(r.get()));
                    return l;
                };
            }

            @Override
            public Function<AtomicReference<TradingMetrics>, TradingMetrics> finisher() {
                return AtomicReference::get;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return emptySet();
            }
        };
    }

}
