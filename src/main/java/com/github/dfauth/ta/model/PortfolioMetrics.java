package com.github.dfauth.ta.model;

import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import static io.github.dfauth.trycatch.Try.tryWith;
import static java.math.BigDecimal.ZERO;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Data
public class PortfolioMetrics {

    private int losingPositions;
    private int winningPositions;
    private long weightedHoldingTime;
    private BigDecimal marketValue = ZERO;
    private BigDecimal purchaseValue = ZERO;
    private BigDecimal saleValue = ZERO;
    private BigDecimal closedProfit = ZERO;
    private BigDecimal dividendIncome = ZERO;
    private int closedPositions;


    public PortfolioMetrics add(PortfolioMetrics other) {
        return new PortfolioMetrics(
                losingPositions + other.losingPositions,
                winningPositions + other.winningPositions,
                weightedHoldingTime + other.weightedHoldingTime,
                marketValue.add(other.marketValue),
                purchaseValue.add(other.purchaseValue),
                saleValue.add(other.saleValue),
                closedProfit.add(other.closedProfit),
                dividendIncome.add(other.dividendIncome),
                closedPositions + other.closedPositions
        );
    }

    public PortfolioMetrics add(Position p) {
        return p.isOpen() ? addOpen((OpenPosition)p) : addClosed(p);
    }
    public PortfolioMetrics addClosed(Position p) {

        return new PortfolioMetrics(
                losingPositions,
                winningPositions,
                weightedHoldingTime,
                marketValue,
                purchaseValue,
                saleValue,
                p.getProfit().map(closedProfit::add).orElse(closedProfit),
                p.getDividends().map(d -> dividendIncome.add(d)).orElse(dividendIncome),
                closedPositions + 1
        );
    }
    public PortfolioMetrics addOpen(OpenPosition p) {

        return new PortfolioMetrics(
                p.isProfitable() ? losingPositions : losingPositions + 1,
                p.isProfitable() ? winningPositions+1 : winningPositions,
                weightedHoldingTime + p.getWeightedHoldingTime(),
                marketValue.add(p.getMarketValue()),
                purchaseValue.add(p.getPurchaseValue()),
                saleValue.add(p.getSaleValue()),
                closedProfit,
                p.getDividends().map(d -> dividendIncome.add(d)).orElse(dividendIncome),
                closedPositions
        );
    }

    public BigDecimal getProfit() {
        return getOpenProfit().add(getClosedProfit()).add(getDividendIncome());
    }

    public BigDecimal getOpenProfit() {
        return getMarketValue().subtract(getPurchaseValue()).add(getSaleValue());
    }

    public Optional<BigDecimal> getReturn() {
        return tryWith(() -> getProfit().divide(getPurchaseValue(), RoundingMode.HALF_UP)).toOptional();
    }

    public int getOpenPositions() {
        return winningPositions + losingPositions;
    }
}
