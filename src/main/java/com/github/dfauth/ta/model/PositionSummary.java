package com.github.dfauth.ta.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.dfauth.ta.functional.Optionals;
import com.github.dfauth.ta.functions.CAGR;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import static com.github.dfauth.ta.functional.Optionals.eitherOrBoth;
import static com.github.dfauth.ta.functions.CAGR.bdMapper;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class PositionSummary {

    private final String code;
    @JsonIgnore
    private Integer closedUnitsPurchased;
    @JsonIgnore
    private Integer openUnitsPurchased;
    @JsonIgnore
    private Integer closedUnitsSold;
    @JsonIgnore
    private Integer openUnitsSold;
    @JsonIgnore
    private Long closedWeightedHoldingTime;
    @JsonIgnore
    private Long openWeightedHoldingTime;
    @JsonIgnore
    private BigDecimal closedPurchaseValue;
    @JsonIgnore
    private BigDecimal openPurchaseValue;
    @JsonIgnore
    private BigDecimal closedSaleValue;
    @JsonIgnore
    private BigDecimal openSaleValue;
    @JsonIgnore
    private BigDecimal closedCommission;
    @JsonIgnore
    private BigDecimal openCommission;
    @JsonProperty("n")
    private int positions;
    @JsonProperty("t")
    private int trades;
    @JsonProperty("op")
    private BigDecimal openProfit;

    public PositionSummary(String code, Price price) {
        this.code = code;
    }

    public PositionSummary add(Position p) {
        return new PositionSummary(
                code,
                p.isClosed() ? eitherOrBoth(closedUnitsPurchased, p.getUnitsPurchased(), Integer::sum).orElse(null) : closedUnitsPurchased,
                p.isOpen() ? eitherOrBoth(openUnitsPurchased, p.getUnitsPurchased(), Integer::sum).orElse(null) : openUnitsPurchased,
                p.isClosed() ? eitherOrBoth(closedUnitsSold, p.getUnitsSold(), Integer::sum).orElse(null) : closedUnitsSold,
                p.isOpen() ? eitherOrBoth(openUnitsSold, p.getUnitsSold(), Integer::sum).orElse(null) : openUnitsSold,
                p.isClosed() ? eitherOrBoth(closedWeightedHoldingTime, p.getWeightedHoldingTime(), Long::sum).orElse(null) : closedWeightedHoldingTime,
                p.isOpen() ? eitherOrBoth(openWeightedHoldingTime, p.getWeightedHoldingTime(), Long::sum).orElse(null) : openWeightedHoldingTime,
                p.isClosed() ? eitherOrBoth(closedPurchaseValue, p.getPurchaseValue(),BigDecimal::add).orElse(null) : closedPurchaseValue,
                p.isOpen() ? eitherOrBoth(openPurchaseValue, p.getPurchaseValue(),BigDecimal::add).orElse(null) : openPurchaseValue,
                p.isClosed() ? eitherOrBoth(closedSaleValue, p.getSaleValue(),BigDecimal::add).orElse(null) : closedSaleValue,
                p.isOpen() ? eitherOrBoth(openSaleValue, p.getSaleValue(),BigDecimal::add).orElse(null) : openSaleValue,
                p.isClosed() ? eitherOrBoth(closedCommission, p.getCommission(),BigDecimal::add).orElse(null) : closedCommission,
                p.isOpen() ? eitherOrBoth(openCommission, p.getCommission(),BigDecimal::add).orElse(null) : openCommission,
                positions + 1,
                trades + p.getTradeCount(),
                p.isOpen() ? Optionals.<BigDecimal>eitherOrBoth(Optional.ofNullable(openProfit), p.getProfit(),BigDecimal::add).orElse(null) : openProfit
        );
    }

    @JsonProperty("osz")
    public Optional<Integer> getOpenSize() {
        return Optional.ofNullable(openUnitsPurchased != null ? openUnitsSold != null ? openUnitsPurchased - openUnitsSold : openUnitsPurchased : null);
    }

    @JsonProperty("cp")
    public Optional<BigDecimal> getClosedProfit() {
        return Optional.ofNullable(closedSaleValue != null ? closedSaleValue.subtract(closedPurchaseValue).subtract(closedCommission) : null);
    }

    @JsonProperty("cr")
    public Optional<Double> getClosedReturn() {
        return getClosedProfit()
                .flatMap(p -> Optional.ofNullable(getClosedPurchaseValue()).filter(pv -> pv.doubleValue() > 0)
                        .map(pv  -> p.divide(pv, RoundingMode.HALF_UP).setScale(3, RoundingMode.HALF_UP).doubleValue()));
    }

    @JsonProperty("or")
    public Optional<Double> getOpenReturn() {
        return Optional.ofNullable(getOpenProfit())
                .flatMap(p -> Optional.ofNullable(getOpenPurchaseValue()).filter(pv -> pv.doubleValue() > 0)
                .map(pv  -> p.divide(pv, RoundingMode.HALF_UP).setScale(3, RoundingMode.HALF_UP).doubleValue()));
    }

    @JsonProperty("ccagr")
    public Optional<Double> getClosedCagr() {
        Optional<Double> periods = Optional.ofNullable(closedWeightedHoldingTime).map(wht -> wht.doubleValue() / (getClosedUnitsPurchased() * 365L));
        return periods.flatMap(p -> getClosedReturn().flatMap(r -> CAGR.cagr(r,p,bdMapper(3)).map(BigDecimal::doubleValue)));
    }

    @JsonProperty("ocagr")
    public Optional<Double> getOpenCagr() {
        return getOpenSize().flatMap(os -> {
            Optional<Double> periods = Optional.ofNullable(openWeightedHoldingTime).map(wht -> wht.doubleValue() / (getOpenUnitsPurchased() * 365L));
            return periods.flatMap(p -> getOpenReturn().flatMap(r -> CAGR.cagr(r,p,bdMapper(3)).map(BigDecimal::doubleValue)));
        });
    }
}
