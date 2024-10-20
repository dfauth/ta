package com.github.dfauth.ta.model;

import com.github.dfauth.ta.util.BigDecimalOps;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;

@Data
public class AggregatedTrade implements TradingMetrics {

    private Timestamp open;
    private Timestamp close;
    private String code;
    private Integer size;
    private BigDecimal profit;
    private BigDecimal turnover;
    private int tradeCount;

    public AggregatedTrade(Trade t) {
        this.open = t.getDate();
        this.code = t.getCode();
        this.size = t.getSide().valueOf(t.getSize());
        this.profit = t.getSide().flip().valueOf(t.getCost()); // we flip to make profit positive and loss negative
        this.turnover = t.getCost(); // always +ve
        this.tradeCount = 1;
    }

    public AggregatedTrade add(Trade t) {
        assert(this.code == t.getCode());
        this.size += t.getSide().valueOf(t.getSize());
        this.profit = this.profit.add(t.getSide().flip().valueOf(t.getCost())); // we flip to make profit positive and loss negative
        this.turnover = this.turnover.add(t.getCost()); // always +ve
        if(isClosed()) {
            this.close = t.getDate();
        }
        this.tradeCount++;
        return this;
    }

    public boolean isClosed() {
        return size == 0;
    }

    @Override
    public int getBreakEvenPositions() {
        return profit.equals(BigDecimal.ZERO) ? 1 : 0;
    }

    @Override
    public int getLosingPositions() {
        return BigDecimalOps.isLessThanZero(profit) ? 1 : 0;
    }

    @Override
    public int getWinningPositions() {
        return BigDecimalOps.isLessThanZero(profit) ? 0 : 1;
    }

    @Override
    public BigDecimal getTotalLoss() {
        return BigDecimalOps.isLessThanZero(profit) ? profit : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getTotalGain() {
        return BigDecimalOps.isLessThanZero(profit) ? BigDecimal.ZERO : profit;
    }

    @Override
    public LocalDate getStart() {
        return open.toLocalDateTime().toLocalDate();
    }

    @Override
    public LocalDate getEnd() {
        return close.toLocalDateTime().toLocalDate();
    }

    @Override
    public long getDuration() {
        return Duration.between(open.toLocalDateTime(), close.toLocalDateTime()).toDays();
    }
}
