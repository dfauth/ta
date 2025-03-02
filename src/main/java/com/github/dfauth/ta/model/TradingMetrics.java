package com.github.dfauth.ta.model;

import com.github.dfauth.ta.functional.Optionals;
import com.github.dfauth.ta.functions.CAGR;
import com.github.dfauth.ta.util.BigDecimalOps;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.github.dfauth.ta.functional.Optionals.*;
import static com.github.dfauth.ta.functions.CAGR.bdMapper;
import static com.github.dfauth.ta.util.BigDecimalOps.valueOf;
import static java.lang.Math.abs;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Getter
public class TradingMetrics {

    public static final Function<Double, Function<Double, Function<Double, Double>>> expectancy = winRate -> avgGain -> avgLoss -> (winRate * avgGain) - (1.0d - winRate * avgLoss);
    public static final BiFunction<Double, Double, Double> riskRewardRatio = (avgGain, avgLoss) -> abs(avgGain / avgLoss);
    public static final BiFunction<Double, Double, Double> roi = (profit, investment) -> profit / investment;
    public static final BiFunction<Double, Double, Double> cagr = CAGR::cagr;
    public static final Function<Double, Function<Double, Function<Double,Double>>> positiveExpectancy = avgWin -> avgLoss -> winRate -> (1 + (avgWin/avgLoss)) * winRate - 1.0d;

    private int losingPositions;
    private int winningPositions;
    private long weightedHoldingTime;
    private int unitsPurchased;
    private BigDecimal purchaseValue;
    private BigDecimal saleValue;
    private BigDecimal totalLoss;
    private BigDecimal totalGain;
    private LocalDate start;
    private LocalDate end;
    private long duration;
    private int positions;
    private int openPositions;
    private int trades;

    public Optional<BigDecimal> getAverageLoss() {
        return bothPresent(getTotalLoss(), getLosingPositions(), (tl,lp) -> tl.divide(valueOf(lp), RoundingMode.HALF_UP));
    }

    public Optional<BigDecimal> getAverageGain() {
        return bothPresent(getTotalGain(), getWinningPositions(), (tg,wp) -> tg.divide(valueOf(wp), RoundingMode.HALF_UP));
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

    public Optional<BigDecimal> getAvergePositionSize() {
        return bothPresent(getPurchaseValue(), getPositions(), BigDecimalOps::divide);
    }

    public Optional<BigDecimal> getAverageTradeSize() {
        return bothPresent(getPurchaseValue(), getTrades(), BigDecimalOps::divide);
    }

    public Optional<Double> getRoi() {
        return getProfit().map(BigDecimal::doubleValue).map(p -> p/getPurchaseValue().doubleValue());
    }

    public Optional<BigDecimal> getProfit() {
        return bothPresent(getTotalGain(),getTotalLoss(), BigDecimal::add);
    }

    public Optional<BigDecimal> getReturn() {
        return eitherOrBoth(getPurchaseValue(),getSaleValue(), BigDecimal::subtract).map(bd -> bd.divide(getPurchaseValue(), RoundingMode.HALF_UP));
    }

    public Optional<BigDecimal> getTurnover() {
        return eitherOrBoth(getPurchaseValue(), getSaleValue(), BigDecimal::add);
    }

    public int getClosedPositions() {
        return positions - openPositions;
    }

    public Optional<Double> getOccupancy() {
        return bothPresent(getStart(), getEnd(), (s,e) -> Duration.between(s.atStartOfDay(), e.atStartOfDay()).toDays())
                .map(elapsed -> (double) getDuration() / (getPositions() * elapsed));
    }

    public Optional<Double> getCagr() {
        double periods = ((double)getWeightedHoldingTime())/(getUnitsPurchased() * 365L);
        return getReturn().map(BigDecimal::doubleValue).filter(r -> periods !=0).flatMap(r -> CAGR.cagr(r,periods,bdMapper(3)).map(BigDecimal::doubleValue));
    }

    public TradingMetrics add(TradingMetrics other) {
        return new TradingMetrics(
                losingPositions + other.losingPositions,
                winningPositions + other.winningPositions,
                weightedHoldingTime + other.weightedHoldingTime,
                unitsPurchased + other.unitsPurchased,
                purchaseValue.add(other.purchaseValue),
                saleValue.add(other.saleValue),
                totalLoss.add(other.totalLoss),
                totalGain.add(other.totalGain),
                start.isBefore(other.start) ? start : other.start,
                end.isAfter(other.end) ? end : other.end,
                duration + other.duration,
                positions + other.positions,
                openPositions + other.openPositions,
                trades + other.trades
        );
    }

    public TradingMetrics add(Position p) {

        return new TradingMetrics(
                p.isProfitable() ? losingPositions : losingPositions + 1,
                p.isProfitable() ? winningPositions+1 : winningPositions,
                weightedHoldingTime + p.getWeightedHoldingTime(),
                unitsPurchased + p.getUnitsPurchased(),
                eitherOrBoth(purchaseValue, p.getPurchaseValue(), BigDecimal::add).orElse(null),
                eitherOrBoth(saleValue, p.getSaleValue(), BigDecimal::add).orElse(null),
                p.isProfitable() ? totalLoss : Optionals.<BigDecimal>eitherOrBoth(Optional.ofNullable(totalLoss), p.getProfit(), BigDecimal::add).orElse(null),
                p.isProfitable() ? Optionals.<BigDecimal>eitherOrBoth(Optional.ofNullable(totalGain), p.getProfit(), BigDecimal::add).orElse(null) : totalGain,
                eitherOrBoth(start, p.getDate().toLocalDateTime().toLocalDate(), (s, d) -> s.isBefore(d) ? s : d).orElse(null),
                eitherOrBoth(end, p.getLast().toLocalDateTime().toLocalDate(), (s, d) -> s.isAfter(d) ? s : d).orElse(null),
                duration + p.getDuration(),
                positions + 1,
                p.isOpen() ? openPositions + 1 : openPositions,
                trades + p.getTrades().size()
        );
    }
}
