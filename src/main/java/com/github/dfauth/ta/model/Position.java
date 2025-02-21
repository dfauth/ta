package com.github.dfauth.ta.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.dfauth.ta.functional.Lists;
import com.github.dfauth.ta.functional.Optionals;
import com.github.dfauth.ta.functions.CAGR;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.github.dfauth.ta.functional.Collectors.oops;
import static com.github.dfauth.ta.functions.CAGR.bdMapper;

@Slf4j
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@IdClass(CodeDateCompositeKey.class)
public class Position {

    @Id @JsonIgnore
    private Timestamp date;
    @Id
    private String code;
    @JsonIgnore
    private Timestamp last;
    @JsonIgnore
    private Integer unitsPurchased;
    @JsonIgnore
    private Integer unitsSold;
    @JsonIgnore
    private Long weightedHoldingTime;
    @JsonProperty("pv")
    private BigDecimal purchaseValue;
    @JsonProperty("sv")
    private BigDecimal saleValue;
    @JsonProperty("c")
    private BigDecimal commission;
    @JsonIgnore
    @OneToMany(targetEntity = Trade.class, orphanRemoval = false)
    private List<Trade> trades;

    public static Long calculateWeightedHoldingTime(Instant start, int size) {
        return calculateWeightedHoldingTime(start, Instant.now(),size);
    }

    public static long calculateWeightedHoldingTime(Instant start, Instant end, int size) {
        return Optionals.bothPresent(start, end, Duration::between).map(Duration::toDays).map(days -> days*size).orElse(0l);
    }

    public Position(Trade t) {
        apply(List.of(t));
    }

    @JsonProperty("s")
    public LocalDate getOpen() {
        return date.toLocalDateTime().toLocalDate();
    }

    @JsonProperty("l")
    public LocalDate getClose() {
        return last.toLocalDateTime().toLocalDate();
    }

    @JsonProperty("t")
    public int getTradeCount() {
        return Optional.ofNullable(trades).map(List::size).orElse(0);
    }

    private void apply(List<Trade> t) {
        List<String> tradeCodes = t.stream().map(Trade::getCode).distinct().toList();
        if(tradeCodes.size() != 1) {
            throw new IllegalArgumentException("list of trades has inconsistent codes: "+t);
        }
        var tradeCode = tradeCodes.get(0);
        if(this.code != null && !this.code.equals(tradeCode)) {
            throw new IllegalArgumentException("trade codes: "+tradeCodes+" does not match position code "+this.code);
        }
        this.code = tradeCode;
        this.date = t.stream().map(Trade::getDate).reduce(this.date, (o, _t) -> o == null ? _t : _t.toInstant().isBefore(o.toInstant()) ? _t : o, oops());
        this.last = t.stream().map(Trade::getDate).reduce(this.last, (o, _t) -> o == null ? _t : _t.toInstant().isAfter(o.toInstant()) ? _t : o, oops());
        t.stream().filter(_t -> this.last != null).forEach(_t -> this.weightedHoldingTime =+ calculateWeightedHoldingTime(this.date.toInstant(), this.last.toInstant(), Optional.ofNullable(getSize()).orElse(0)));
        this.unitsPurchased = t.stream().filter(_t -> _t.getSide().isBuy()).mapToInt(Trade::getSize).reduce(this.unitsPurchased == null ? 0 : this.unitsPurchased, Integer::sum);
        this.unitsSold = t.stream().filter(_t -> _t.getSide().isSell()).mapToInt(Trade::getSize).reduce(this.unitsSold == null ? 0 : this.unitsSold, Integer::sum);
        this.purchaseValue = t.stream().filter(_t -> _t.getSide().isBuy()).map(Trade::getCost).reduce(this.purchaseValue, (c, _t) -> c == null ? _t : c.add(_t), oops());
        this.saleValue = t.stream().filter(_t -> _t.getSide().isSell()).map(Trade::getCost).reduce(this.saleValue, (c, _t) -> c == null ? _t : c.add(_t), oops());
        this.commission = t.stream().map(Trade::getCommission).reduce(this.commission, (c, _t) -> c == null ? _t : c.add(_t), oops());
        this.trades = Lists.add(this.trades, t);
    }

    public Position onTrade(Trade t) {
        apply(List.of(t));
        return this;
    }

    public Position later(Position other) {
        if(!code.equals(other.getCode())) {
            throw new IllegalArgumentException("Cant aggregate positions across codes: "+code+" and "+other.getCode());
        }
        return new Position(
                this.date.toInstant().isBefore(other.date.toInstant()) ? this.date : other.date,
                this.code,
                this.last.toInstant().isAfter(other.last.toInstant()) ? this.last : other.last,
                this.unitsPurchased + other.unitsPurchased,
                this.unitsSold + other.unitsSold,
                this.weightedHoldingTime + other.weightedHoldingTime,
                this.purchaseValue.add(other.purchaseValue),
                this.saleValue.add(other.saleValue),
                this.commission.add(other.commission),
                this.trades = Lists.add(this.trades, other.trades)
        );
    }

    @JsonIgnore
    public boolean isClosed() {
        return getSize() != null && getSize() == 0;
    }

    @JsonIgnore
    public boolean isOpen() {
        return getSize() != null && !isClosed();
    }

    @JsonProperty("sz")
    public Integer getSize() {
        return unitsPurchased != null ? unitsSold != null ? unitsPurchased - unitsSold : unitsPurchased : null;
    }

    @JsonProperty("p")
    public BigDecimal getProfit() {
        return saleValue != null ? saleValue.subtract(purchaseValue).subtract(commission) : null;
    }

    @JsonProperty("r")
    public Optional<Double> getReturn() {
        return Optional.ofNullable(getProfit()).flatMap(p -> Optional.ofNullable(getPurchaseValue()).filter(pv -> pv.doubleValue() > 0).map(pv  -> p.divide(pv, RoundingMode.HALF_UP).doubleValue()));
    }

    @JsonProperty("cagr")
    public Optional<Double> getCagr() {
        double periods = ((double)getWeightedHoldingTime())/(getUnitsPurchased() * 365L);
        return getReturn().filter(r -> periods !=0).flatMap(r -> CAGR.cagr(r,periods,bdMapper(3)).map(BigDecimal::doubleValue));
    }
}
