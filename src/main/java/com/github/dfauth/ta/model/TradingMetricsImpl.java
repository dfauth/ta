package com.github.dfauth.ta.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
public class TradingMetricsImpl implements TradingMetrics {
    private final int tradeCount;
    private final int breakEvenPositions;
    private final int losingPositions;
    private final int winningPositions;
    private final BigDecimal turnover;
    private final BigDecimal totalLoss;
    private final BigDecimal totalGain;
    private final LocalDate start;
    private final LocalDate end;
    private final long duration;
    private final List<AggregatedTrade> aggregatedTrades;
}
