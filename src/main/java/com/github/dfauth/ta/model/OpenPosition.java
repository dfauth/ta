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
            position.getTrades());
        this.price = price;
        this.progression = onLoad(trades);
    }

    @Override
    public Optional<BigDecimal> getProfit() {
        var marketValue = price.getClose().multiply(BigDecimal.valueOf(getSize()));
        Function<BigDecimal, Function<BigDecimal, Function<BigDecimal, BigDecimal>>> f = mv -> pv -> c -> mv.subtract(pv).subtract(c);
        return super.getProfit()
                .map(p -> p.add(marketValue))
                .or(() -> allPresent(f, marketValue,getPurchaseValue(),getCommission()));
    }

    @Override
    public long getWeightedHoldingTime() {
        return getT(Position::getWeightedHoldingTime).orElseThrow();
    }

    @Override
    public LocalDate getClose() {
        return getT(Position::getClose).orElseThrow();
    }

    @Override
    public int getSize() {
        return getT(Position::getSize).orElseThrow();
    }

    @Override
    public int getUnitsPurchased() {
        return getT(Position::getUnitsPurchased).orElseThrow();
    }

    @Override
    public int getUnitsSold() {
        return getT(Position::getUnitsSold).orElseThrow();
    }

    @Override
    public BigDecimal getPurchaseValue() {
        return getT(Position::getPurchaseValue).orElseThrow();
    }

    @Override
    public BigDecimal getSaleValue() {
        return getT(Position::getSaleValue).orElseThrow();
    }

    @Override
    public BigDecimal getCommission() {
        return getT(Position::getCommission).orElseThrow();
    }

    @Override
    public int getTradeCount() {
        return getT(Position::getTradeCount).orElseThrow();
    }

    private <T> Optional<T> getT(Function<Position,T> f) {
        return Optional.ofNullable(progression.floorEntry(price.getDate())).map(Map.Entry::getValue).map(f);
    }
}
