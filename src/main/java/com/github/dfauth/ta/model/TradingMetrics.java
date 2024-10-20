package com.github.dfauth.ta.model;

import com.github.dfauth.ta.functional.Maps;
import com.github.dfauth.ta.util.BigDecimalOps;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    default int getTotalPositions() {
        return getWinningPositions() + getLosingPositions() + getBreakEvenPositions();
    }

    default BigDecimal getAverageLoss() {
        return BigDecimalOps.divide(getTotalLoss(), getLosingPositions());
    }

    default BigDecimal getAverageGain() {
        return BigDecimalOps.divide(getTotalGain(), getWinningPositions());
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

    default double getExpectancyRatio() {
        return (getWinRate()/(1-getWinRate()))*getRiskRewardRatio();
    }

    default BigDecimal getAvergePositionSize() {
        return BigDecimalOps.divide(getTurnover(), (2*getTotalPositions()));
    }

    default BigDecimal getAverageTradeSize() {
        return BigDecimalOps.divide(getTurnover(), (2*(getTradeCount() - getTotalPositions())));
    }

    default double getRoi() {
        return getExpectancy() / getAvergePositionSize().doubleValue();
    }

    default double getOccupancy() {
        return (double) getDuration() / (getTotalPositions() * Duration.between(getStart().atStartOfDay(), getEnd().atStartOfDay()).toDays());
    }

    default double getCagr() {
        return cagr(getRoi(), (double)Duration.between(getStart().atStartOfDay(), getEnd().atStartOfDay()).toDays()/365);
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
                getDuration() + tm.getDuration()
        );
    }

    static Optional<TradingMetrics> calculateTradingMetrics(Map<String, List<Trade>> tradeMap) {
        // step 1 aggregate individual trades into positions
        Map<String, TradeAccumulator> positionMap = Maps.mapValues(tradeMap, TradingMetrics::aggregate);
        // step 2 filter open positions
        Map<String, List<AggregatedTrade>> closedPositions = Maps.mapValues(positionMap, TradeAccumulator::getAggregatedTrades);
        // aggregate across securities
        Optional<TradingMetrics> tradingMetrics = closedPositions.values().stream()
                .flatMap(List::stream).map(TradingMetrics.class::cast).reduce(TradingMetrics::merge);
        return tradingMetrics;
    }

    private static TradeAccumulator aggregate(List<Trade> values) {
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
