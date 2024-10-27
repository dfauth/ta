package com.github.dfauth.ta.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.github.dfauth.ta.util.BigDecimalOps.multiply;

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PortfolioSummary {

    private BigDecimal cash = BigDecimal.ZERO;
    private int count = 0;
    private BigDecimal cost = BigDecimal.ZERO;
    private BigDecimal commission = BigDecimal.ZERO;
    private BigDecimal marketValue = BigDecimal.ZERO;
    private List<PositionWithMarketValue> positions = new ArrayList<>();

    public PortfolioSummary withCash(BigDecimal cash) {
        this.cash = cash;
        return this;
    }

    public PortfolioSummary withPosition(Position p, BigDecimal marketPrice) {
        this.count++;
        this.cost = this.cost.add(p.getCost());
        this.commission = this.commission.add(p.getCommission());
        BigDecimal _marketValue = multiply(marketPrice, p.getSize());
        this.positions.add(new PositionWithMarketValue(p, _marketValue));
        this.marketValue = this.marketValue.add(_marketValue);
        return this;
    }

    public BigDecimal getTotalValue() {
        return marketValue.add(cash);
    }

    public BigDecimal getTotalCost() {
        return cost.add(commission);
    }

    public PortfolioSummary compare(PortfolioSummary prev) {
        Set<PositionWithMarketValue> p = new HashSet<>(positions);
        p.removeAll(new HashSet<>(prev.getPositions()));
        return new PortfolioSummary(
                cash.subtract(prev.cash),
                count - prev.getCount(),
                cost.subtract(prev.getCost()),
                commission.subtract(prev.getCommission()),
                marketValue.subtract(prev.marketValue),
                new ArrayList<>(p)
                );
    }

    public static class PositionWithMarketValue extends Position {

        private final BigDecimal marketValue;

        public PositionWithMarketValue(Position p, BigDecimal marketValue) {
            super(p.getDate(), p.getCode(), p.getSize(), p.getCost(), p.getCommission());
            this.marketValue = marketValue;
        }

        public BigDecimal getProfit() {
            return marketValue.subtract(getCost());
        }
    }
}
