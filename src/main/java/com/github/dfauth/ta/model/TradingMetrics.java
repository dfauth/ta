package com.github.dfauth.ta.model;

import com.github.dfauth.ta.functional.Maps;
import com.github.dfauth.ta.util.BigDecimalOps;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static com.github.dfauth.ta.functional.Collectors.oops;
import static com.github.dfauth.ta.functions.CAGR.cagr;

public interface TradingMetrics {

    int getTradeCount();
    int getBreakEvenPositions();
    int getLosingPositions();
    int getWinningPositions();
    BigDecimal getTurnover();
    BigDecimal getTotalLoss();
    BigDecimal getTotalGain();
    LocalDate getStart();
    LocalDate getEnd();
    long getDuration();
    //List<AggregatedTrade> getAggregatedTrades();

    default int getTotalPositions() {
        return getWinningPositions() + getLosingPositions() + getBreakEvenPositions();
    }

    default BigDecimal getAverageLoss() {
        return BigDecimalOps.divideWithZeroCheck(getTotalLoss(), getLosingPositions()).orElse(BigDecimal.ZERO);
    }

    default BigDecimal getAverageGain() {
        return BigDecimalOps.divideWithZeroCheck(getTotalGain(), getWinningPositions()).orElse(BigDecimal.ZERO);
    }

    default double getWinRate() {
        return (double) getWinningPositions() / getTotalPositions();
    }

    default double getExpectancy() {
        return (getWinRate()*getAverageGain().doubleValue()) - ((1 - getWinRate())*getAverageLoss().doubleValue());
    }

    default double getRiskRewardRatio() {
        return getAverageGain().doubleValue() / (-1.0 * getAverageLoss().doubleValue());
    }

    @Slf4j
    public static class Logger {}
    default double getPositiveExpectancy() {
        double w = getAverageGain().doubleValue();
        double l = getAverageLoss().doubleValue() * -1d;
        double p = getWinRate();
        return l == 0 ? 0.0 :
                (1 + (w/l)) * p - 1.0d;
    }

    default BigDecimal getAvergePositionSize() {
        return BigDecimalOps.divide(getCostBase(), getTotalPositions());
    }

    default BigDecimal getAverageTradeSize() {
        return BigDecimalOps.divide(getTurnover(), getTradeCount());
    }

    default double getRoi() {
        return getExpectancy() / getAvergePositionSize().doubleValue();
    }

    default BigDecimal getCostBase() {
        return BigDecimalOps.divide(getTurnover().subtract(getProfit()),2);
    }

    default BigDecimal getProfit() {
        return getTotalGain().add(getTotalLoss());
    }

    default double getOccupancy() {
        return (double) getDuration() / (getTotalPositions() * Duration.between(getStart().atStartOfDay(), getEnd().atStartOfDay()).toDays());
    }

    default double getCagr() {
        return cagr(getRoi(), (double) getDuration() /(365*getTotalPositions()));
    }

    default TradingMetrics merge(TradingMetrics tm) {
        return new TradingMetricsImpl(
                getTradeCount() + tm.getTradeCount(),
                getBreakEvenPositions() + tm.getBreakEvenPositions(),
                getLosingPositions() + tm.getLosingPositions(),
                getWinningPositions() + tm.getWinningPositions(),
                getTurnover().add(tm.getTurnover()),
                getTotalLoss().add(tm.getTotalLoss()),
                getTotalGain().add(tm.getTotalGain()),
                getStart().isBefore(tm.getStart()) ? getStart() : tm.getStart(),
                getEnd().isAfter(tm.getStart()) ? getEnd() : tm.getEnd(),
                getDuration() + tm.getDuration(),
                List.of() //Lists.add(getAggregatedTrades(), tm.getAggregatedTrades())
        );
    }

    static Optional<TradingMetrics> openTradingMetrics(Map<String, TradeAccumulator> positionMap, Function<String, BigDecimal> priceCallback) {

        // step 2a filter open positions
        Map<String, AggregatedTrade> openPositions = Maps.of(positionMap).mapValues(TradeAccumulator::getOpenTrade).mapValues(t -> t.closeAt(priceCallback.apply(t.getCode())));

        // aggregate across securities
        return openPositions.values().stream()
                .map(TradingMetrics.class::cast).reduce(TradingMetrics::merge);
    }

    static Optional<TradingMetrics> closedTradingMetrics(Map<String, TradeAccumulator> positionMap) {

        // step 2b filter closed positions
        Map<String, List<AggregatedTrade>> closedPositions = Maps.mapValues(positionMap, TradeAccumulator::getAggregatedTrades);

        // aggregate across securities
        return closedPositions.values().stream()
                .flatMap(List::stream).map(TradingMetrics.class::cast).reduce(TradingMetrics::merge);
    }

    static TradeAccumulator aggregate(List<Trade> values) {
        return values.stream().reduce(new TradeAccumulator(), TradeAccumulator::add, oops());
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    class TradeAccumulator {

        private List<AggregatedTrade> aggregatedTrades = new ArrayList<>();
        private AggregatedTrade openTrade = null;

        public TradeAccumulator add(Trade t) {
            openTrade = Optional.ofNullable(openTrade).map(_t -> _t.add(t)).orElse(new AggregatedTrade(t));
            if(openTrade.isClosed()) {
                aggregatedTrades.add(openTrade);
                openTrade = null;
            }
            return this;
        }
    }
}
