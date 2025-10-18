package com.github.dfauth.ta.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.dfauth.ta.util.BigDecimalOps;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface Position {

    @JsonProperty("code")
    String getCode();

    @JsonProperty("s")
    LocalDate getOpen();

    @JsonProperty("l")
    LocalDate getLast();

    @JsonProperty("cl")
    default Optional<LocalDate> getClose() {
        return Optional.of(getLast()).filter(l -> isClosed());
    }

    @JsonIgnore
    default boolean isOpen() {
        return !isClosed();
    }

    @JsonIgnore
    default boolean isClosed() {
        return getSize() == 0;
    }

    @JsonProperty("sz")
    int getSize();

    @JsonProperty("p")
    Optional<BigDecimal> getProfit();

    @JsonProperty("d")
    Optional<BigDecimal> getDividends();

    @JsonProperty("t")
    default int getTradeCount() {
        return getTrades().size();
    }

    @JsonIgnore
    List<Trade> getTrades();

    @JsonIgnore
    default boolean isProfitable() {
        return getProfit().map(BigDecimalOps::isGreaterThanZero).orElse(false);
    }

    @JsonProperty("duration")
    long getDuration();

    @JsonProperty("wht")
    BigDecimal getWeightedHoldingTime();

    @JsonIgnore
    int getUnitsPurchased();

    @JsonProperty("pv")
    BigDecimal getPurchaseValue();

    @JsonProperty("sv")
    BigDecimal getSaleValue();

    @JsonProperty("c")
    BigDecimal getCommission();

    @JsonProperty("mv")
    Optional<BigDecimal> getMarketValue();
}
