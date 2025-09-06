package com.github.dfauth.ta.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;

import static com.github.dfauth.ta.functional.Optionals.allPresent;
import static java.math.BigDecimal.ZERO;

@Getter
@ToString
@EqualsAndHashCode
public class OpenPosition extends Position {

    @JsonIgnore
    private Price price;
    @JsonIgnore
    private final TreeMap<LocalDate, Position> progression;

    public OpenPosition(Position position, Price price) {
        super(position.getDate(),
            position.getCode(),
            position.getLast(),
            position.getUnitsPurchased(),
            position.getUnitsSold(),
            position.getWeightedHoldingTime(),
            position.getPurchaseValue(),
            position.getSaleValue(),
            position.getCommission(),
            position.getTrades(),
            position.getPayments());
        this.price = price;
        this.progression = onLoad(trades);
    }

    @Override
    public Optional<BigDecimal> getProfit() {
        Function<BigDecimal, Function<BigDecimal, Function<BigDecimal, BigDecimal>>> f = mv -> pv -> c -> mv.subtract(pv).subtract(c);
        return super.getProfit()
                .map(p -> p.add(getMarketValue()))
                .or(() -> allPresent(f,getMarketValue(),getPurchaseValue(),getCommission()));
    }

    @Override
    public long getWeightedHoldingTime() {
        return getT(Position::getWeightedHoldingTime).orElse(0l);
    }

    @Override
    public LocalDate getClose() {
        return getT(Position::getClose).orElse(null);
    }

    @Override
    public int getSize() {
        return getT(Position::getSize).orElse(0);
    }

    @Override
    public int getUnitsPurchased() {
        return getT(Position::getUnitsPurchased).orElse(0);
    }

    @Override
    public int getUnitsSold() {
        return getT(Position::getUnitsSold).orElse(0);
    }

    @Override
    public BigDecimal getPurchaseValue() {
        return getT(Position::getPurchaseValue).orElse(ZERO);
    }

    @Override
    public BigDecimal getSaleValue() {
        return getT(Position::getSaleValue).orElse(ZERO);
    }

    @Override
    public BigDecimal getCommission() {
        return getT(Position::getCommission).orElse(ZERO);
    }

    @Override
    public int getTradeCount() {
        return getT(Position::getTradeCount).orElse(0);
    }

    @Override
    public Optional<Double> getCagr() {
        long openWeightedHoldingTime = calculateWeightedHoldingTime(getLast().toInstant(), getSize());
        Optional<Double> p = periods.apply(getWeightedHoldingTime() + openWeightedHoldingTime, getUnitsPurchased()).or(() -> Optional.of(((double)openWeightedHoldingTime) / getUnitsPurchased()));
        return p.flatMap(_p -> getReturn()
                .flatMap(_r -> cagr.apply(_r,_p)));
    }

    private <T> Optional<T> getT(Function<Position,T> f) {
        return Optional.ofNullable(progression.floorEntry(price.getDate())).map(Map.Entry::getValue).map(f);
    }

    public BigDecimal getMarketValue() {
        return price.getClose().multiply(BigDecimal.valueOf(getSize()));
    }
}
