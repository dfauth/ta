package com.github.dfauth.ta.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import static com.github.dfauth.ta.functional.Optionals.eitherOrBoth;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class PositionSummary {

    private final String code;
    @JsonIgnore
    private final Price price;
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

    public PositionSummary(String code, Price price) {
        this.code = code;
        this.price = price;
    }

    public PositionSummary add(Position p) {
        return new PositionSummary(
                code,
                price,
                p.isClosed() ? eitherOrBoth(closedUnitsPurchased, p.getUnitsPurchased(), Integer::sum) : closedUnitsPurchased,
                p.isOpen() ? eitherOrBoth(openUnitsPurchased, p.getUnitsPurchased(), Integer::sum) : openUnitsPurchased,
                p.isClosed() ? eitherOrBoth(closedUnitsSold, p.getUnitsSold(), Integer::sum) : closedUnitsSold,
                p.isOpen() ? eitherOrBoth(openUnitsSold, p.getUnitsSold(), Integer::sum) : openUnitsSold,
                p.isClosed() ? eitherOrBoth(closedWeightedHoldingTime, p.getWeightedHoldingTime(), Long::sum) : closedWeightedHoldingTime,
                p.isOpen() ? eitherOrBoth(openWeightedHoldingTime, p.getWeightedHoldingTime(), Long::sum) : openWeightedHoldingTime,
                p.isClosed() ? eitherOrBoth(closedPurchaseValue, p.getPurchaseValue(),(v1,v2) -> v2.add(v1)) : closedPurchaseValue,
                p.isOpen() ? eitherOrBoth(openPurchaseValue, p.getPurchaseValue(),(v1,v2) -> v2.add(v1)) : openPurchaseValue,
                p.isClosed() ? eitherOrBoth(closedSaleValue, p.getSaleValue(),(v1,v2) -> v2.add(v1)) : closedSaleValue,
                p.isOpen() ? eitherOrBoth(openSaleValue, p.getSaleValue(),(v1,v2) -> v2.add(v1)) : openSaleValue,
                p.isClosed() ? eitherOrBoth(closedCommission, p.getCommission(),(v1,v2) -> v2.add(v1)) : closedCommission,
                p.isOpen() ? eitherOrBoth(openCommission, p.getCommission(),(v1,v2) -> v2.add(v1)) : openCommission
        );
    }

    @JsonProperty("csz")
    public Optional<Integer> getClosedSize() {
        return Optional.ofNullable(closedUnitsPurchased != null ? closedUnitsSold != null ? closedUnitsPurchased - closedUnitsSold : closedUnitsPurchased : null);
    }

    @JsonProperty("osz")
    public Optional<Integer> getOpenSize() {
        return Optional.ofNullable(openUnitsPurchased != null ? openUnitsSold != null ? openUnitsPurchased - openUnitsSold : openUnitsPurchased : null);
    }

    @JsonProperty("cp")
    public Optional<BigDecimal> getClosedProfit() {
        return Optional.ofNullable(closedSaleValue != null ? closedSaleValue.subtract(closedPurchaseValue).subtract(closedCommission) : null);
    }

    @JsonProperty("op")
    public Optional<BigDecimal> getOpenProfit() {
        return Optional.ofNullable(openSaleValue != null ? openSaleValue.subtract(openPurchaseValue).subtract(openCommission) : null);
    }

    @JsonProperty("cr")
    public Optional<Double> getClosedReturn() {
        return getClosedProfit().flatMap(p -> Optional.ofNullable(getClosedPurchaseValue()).filter(pv -> pv.doubleValue() > 0).map(pv  -> p.divide(pv, RoundingMode.HALF_UP).doubleValue()));
    }

    @JsonProperty("or")
    public Optional<Double> getOpenReturn() {
        return getOpenProfit().flatMap(p -> Optional.ofNullable(getOpenPurchaseValue()).filter(pv -> pv.doubleValue() > 0).map(pv  -> p.divide(pv, RoundingMode.HALF_UP).doubleValue()));
    }

    @JsonProperty("ccagr")
    public Optional<Double> getClosedCagr() {
        return getClosedSize().flatMap(cs -> {
            Optional<Double> periods = Optional.ofNullable(closedWeightedHoldingTime).map(wht -> wht.doubleValue() / (getClosedUnitsPurchased() * 365L));
            return periods.flatMap(p -> getClosedReturn().filter(r -> p>1).map(r -> Math.pow(r, (double) 1 / (p-1))));
        });
    }

    @JsonProperty("ocagr")
    public Optional<Double> getOpenCagr() {
        return getOpenSize().flatMap(os -> {
            Optional<Double> periods = Optional.ofNullable(openWeightedHoldingTime).map(wht -> wht.doubleValue() / (getOpenUnitsPurchased() * 365L));
            return periods.flatMap(p -> getOpenReturn().filter(r -> p>1).map(r -> Math.pow(r, (double) 1 / (p-1))));
        });
    }
}
