package com.github.dfauth.ta.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.dfauth.ta.functional.Optionals;
import com.github.dfauth.ta.functions.CAGR;
import com.github.dfauth.ta.repo.Position;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.function.Function;

import static com.github.dfauth.ta.functions.CAGR.bdMapper;
import static com.github.dfauth.ta.repo.Position.nonNaN;
import static com.github.dfauth.ta.repo.Position.nonZero;
import static com.github.dfauth.ta.util.Utils.thenThrow;
import static java.math.BigDecimal.ZERO;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class PositionSummary {

    private String code;
    @JsonIgnore
    private int closedUnitsPurchased;
    @JsonIgnore
    private int openUnitsPurchased;
    @JsonIgnore
    private int closedUnitsSold;
    @JsonIgnore
    private int openUnitsSold;
    @JsonIgnore
    private long closedWeightedHoldingTime;
    @JsonIgnore
    private long openWeightedHoldingTime;
    @JsonIgnore
    private BigDecimal closedPurchaseValue = ZERO;
    @JsonIgnore
    private BigDecimal openPurchaseValue = ZERO;
    @JsonIgnore
    private BigDecimal closedSaleValue = ZERO;
    @JsonIgnore
    private BigDecimal openSaleValue = ZERO;
    @JsonIgnore
    private BigDecimal closedCommission = ZERO;
    @JsonIgnore
    private BigDecimal openCommission = ZERO;
    @JsonProperty("d")
    private BigDecimal dividends = ZERO;
    @JsonProperty("n")
    private int positions;
    @JsonProperty("t")
    private int trades;
    @JsonProperty("op")
    private BigDecimal openProfit = ZERO;

    public PositionSummary add(Position p) {
        var result = new PositionSummary(
                Optional.ofNullable(code).map(p.getCode()::equals).orElse(true) ? code : thenThrow(() -> new IllegalStateException("codes dont match: "+code+"and "+p.getCode())),
                p.isClosed() ? closedUnitsPurchased + p.getUnitsPurchased() : closedUnitsPurchased,
                p.isOpen() ? openUnitsPurchased + p.getUnitsPurchased() : openUnitsPurchased,
                p.isClosed() ? closedUnitsSold + p.getUnitsSold() : closedUnitsSold,
                p.isOpen() ? openUnitsSold + p.getUnitsSold() : openUnitsSold,
                p.isClosed() ? closedWeightedHoldingTime + p.getUnitsHoldingDays() : closedWeightedHoldingTime,
                p.isOpen() ? openWeightedHoldingTime + p.getUnitsHoldingDays() : openWeightedHoldingTime,
                p.isClosed() ? closedPurchaseValue.add(p.getPurchaseValue()) : closedPurchaseValue,
                p.isOpen() ? openPurchaseValue.add(p.getPurchaseValue()) : openPurchaseValue,
                p.isClosed() ? closedSaleValue.add(p.getSaleValue()) : closedSaleValue,
                p.isOpen() ? openSaleValue.add(p.getSaleValue()) : openSaleValue,
                p.isClosed() ? closedCommission.add(p.getCommission()) : closedCommission,
                p.isOpen() ? openCommission.add(p.getCommission()) : openCommission,
                p.getDividends().map(dividends::add).orElse(dividends),
                positions + 1,
                trades + p.getTradeCount(),
                p.isOpen() ? Optionals.<BigDecimal>eitherOrBoth(Optional.ofNullable(openProfit), p.getProfit(),BigDecimal::add).orElse(null) : openProfit
        );
        return result;
    }

    @JsonProperty("osz")
    public int getOpenSize() {
        return openUnitsPurchased - openUnitsSold;
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
        Optional<Double> periods = Optional.of(openWeightedHoldingTime)
                .map(wht -> wht.doubleValue() / (getOpenUnitsPurchased() * 365L))
                .filter(nonZero)
                .filter(nonNaN);
        Function<Double,Optional<Double>> cagr = r -> periods.flatMap(p -> CAGR.cagr(r, p, bdMapper(3))).map(BigDecimal::doubleValue);
        return periods.flatMap(p -> getOpenReturn().flatMap(cagr));
    }

    @JsonProperty("y")
    public double getYield() {
        return 0.0;
    }
}
