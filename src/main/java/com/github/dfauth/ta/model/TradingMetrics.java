package com.github.dfauth.ta.model;

import com.github.dfauth.ta.functions.CAGR;
import com.github.dfauth.ta.util.BigDecimalOps;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;

import static com.github.dfauth.ta.functional.Optionals.eitherOrBoth;
import static com.github.dfauth.ta.functions.CAGR.bdMapper;
import static java.math.BigDecimal.ZERO;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Getter
public class TradingMetrics {

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
    private int trades;

    public BigDecimal getAverageLoss() {
        return BigDecimalOps.divideWithZeroCheck(getTotalLoss(), getLosingPositions()).orElse(ZERO);
    }

    public BigDecimal getAverageGain() {
        return BigDecimalOps.divideWithZeroCheck(getTotalGain(), getWinningPositions()).orElse(ZERO);
    }

    public double getWinRate() {
        return (double) getWinningPositions() / getPositions();
    }

    public double getExpectancy() {
        return (getWinRate()*getAverageGain().doubleValue()) - ((1 - getWinRate())*getAverageLoss().doubleValue());
    }

    public double getRiskRewardRatio() {
        return getAverageGain().doubleValue() / (-1.0 * getAverageLoss().doubleValue());
    }

    public double getPositiveExpectancy() {
        double w = getAverageGain().doubleValue();
        double l = getAverageLoss().doubleValue() * -1d;
        double p = getWinRate();
        return l == 0 ? 0.0 :
                (1 + (w/l)) * p - 1.0d;
    }

    public BigDecimal getAvergePositionSize() {
        return BigDecimalOps.divide(getPurchaseValue(), getPositions());
    }

    public BigDecimal getAverageTradeSize() {
        return BigDecimalOps.divide(getPurchaseValue(), getTrades());
    }

    public double getRoi() {
        return getProfit().doubleValue()/getPurchaseValue().doubleValue();
    }

    public BigDecimal getProfit() {
        return getTotalGain().add(getTotalLoss());
    }

    public BigDecimal getReturn() {
        return eitherOrBoth(getPurchaseValue(),getSaleValue(), BigDecimal::subtract).divide(getPurchaseValue(), RoundingMode.HALF_UP);
    }

    public BigDecimal getTurnover() {
        return eitherOrBoth(getPurchaseValue(), getSaleValue(), BigDecimal::add);
    }

    public double getOccupancy() {
        return (double) getDuration() / (getPositions() * Duration.between(getStart().atStartOfDay(), getEnd().atStartOfDay()).toDays());
    }

    public Optional<Double> getCagr() {
        double periods = ((double)getWeightedHoldingTime())/(getUnitsPurchased() * 365L);
//        double periods = ((double)getDuration())/(getPositions() * 365L);
        return Optional.ofNullable(getReturn()).map(BigDecimal::doubleValue).filter(r -> periods !=0).flatMap(r -> CAGR.cagr(r,periods,bdMapper(3)).map(BigDecimal::doubleValue));
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
                trades + other.trades
        );
    }

    public TradingMetrics add(Position p) {

        return new TradingMetrics(
                p.isProfitable() ? losingPositions : losingPositions + 1,
                p.isProfitable() ? winningPositions+1 : winningPositions,
                weightedHoldingTime + p.getWeightedHoldingTime(),
                unitsPurchased + p.getUnitsPurchased(),
                eitherOrBoth(purchaseValue, p.getPurchaseValue(), BigDecimal::add),
                eitherOrBoth(saleValue, p.getSaleValue(), BigDecimal::add),
                p.isProfitable() ? totalLoss : eitherOrBoth(totalLoss, p.getProfit(), BigDecimal::add),
                p.isProfitable() ? eitherOrBoth(totalGain, p.getProfit(), BigDecimal::add) : totalGain,
                eitherOrBoth(start, p.getDate().toLocalDateTime().toLocalDate(), (s, d) -> s.isBefore(d) ? s : d),
                eitherOrBoth(end, p.getLast().toLocalDateTime().toLocalDate(), (s, d) -> s.isAfter(d) ? s : d),
                duration + p.getDuration(),
                positions + 1,
                trades + p.getTrades().size()
        );
    }
}
