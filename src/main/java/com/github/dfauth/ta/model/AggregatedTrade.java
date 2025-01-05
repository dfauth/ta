package com.github.dfauth.ta.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dfauth.ta.util.BigDecimalOps;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
public class AggregatedTrade implements TradingMetrics {

    private Timestamp open;
    private Timestamp close;
    private String code;
    private Integer size;
    private BigDecimal costBasis;
    private BigDecimal saleValue;
    private BigDecimal commission;
    private int tradeCount;

    public AggregatedTrade(Trade t) {
        this.open = t.getDate();
        this.code = t.getCode();
        this.size = t.getSide().valueOf(t.getSize());
        this.costBasis = t.getSide().isBuy()  ? t.getCost() : BigDecimal.ZERO; // always +ve
        this.saleValue = t.getSide().isSell() ? t.getSide().flip().valueOf(t.getCost()) : BigDecimal.ZERO; // seel cost is -ve, so we flip
        this.commission = t.getCommission();
        this.tradeCount = 1;
    }

    public AggregatedTrade add(Trade t) {
        assert(this.code == t.getCode());
        this.size += t.getSide().valueOf(t.getSize());
        this.costBasis = t.getSide().isBuy() ? this.costBasis.add(t.getCost()) : this.costBasis; // always +ve
        this.saleValue = t.getSide().isSell() ? this.saleValue.add(t.getSide().flip().valueOf(t.getCost())) : this.saleValue; // we flip to make profit positive and loss negative
        this.commission = this.commission.add(t.getCommission());
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
        return getProfit().equals(BigDecimal.ZERO) ? 1 : 0;
    }

    @Override
    public int getLosingPositions() {
        return BigDecimalOps.isLessThanZero(getProfit()) ? 1 : 0;
    }

    @Override
    public int getWinningPositions() {
        return BigDecimalOps.isLessThanZero(getProfit()) ? 0 : 1;
    }

    @Override
    public BigDecimal getTurnover() {
        return saleValue.add(costBasis);
    }

    @Override
    public BigDecimal getTotalLoss() {
        return BigDecimalOps.isLessThanZero(getProfit()) ? getProfit() : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getTotalGain() {
        return BigDecimalOps.isLessThanZero(getProfit()) ? BigDecimal.ZERO : getProfit();
    }

    @Override
    public BigDecimal getProfit() {
        return saleValue.subtract(costBasis).subtract(commission);
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

//    @Override
    @JsonIgnore
    public List<AggregatedTrade> getAggregatedTrades() {
        return List.of(this);
    }

    public AggregatedTrade closeAt(BigDecimal price) {
        this.saleValue = this.saleValue.add(BigDecimalOps.multiply(price, size));
        this.size = 0;
        this.close = Timestamp.from(Instant.now());
        return this;
    }
}
