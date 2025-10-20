package com.github.dfauth.ta.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;

import static com.github.dfauth.ta.functional.Optionals.eitherOrBoth;
import static com.github.dfauth.ta.util.BigDecimalOps.isGreaterThanZero;
import static java.util.Optional.empty;

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

    @JsonProperty("tp")
    default Optional<BigDecimal> getTradingProfit() {
        return isOpen() ? empty() : Optional.of(getSaleValue().add(getPurchaseValue()));
    }

    @JsonProperty("p")
    default Optional<BigDecimal> getProfit() {
        return isOpen() ? getDividends() : eitherOrBoth(getDividends(), getTradingProfit(), (BinaryOperator<BigDecimal>) BigDecimal::add);
    }

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
        return isGreaterThanZero(getProfit().orElseGet(() -> getMarketValue()
                        .map(mv -> mv.add(getPurchaseValue()))
                        .orElseThrow()
                ));
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
