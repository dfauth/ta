package com.github.dfauth.ta.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.OneToMany;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Function;

import static com.github.dfauth.ta.functional.Optionals.allPresent;
import static java.math.BigDecimal.ZERO;

@Getter
@ToString
@EqualsAndHashCode
public class OpenPosition extends Position {

    @JsonIgnore
    @OneToMany(targetEntity = Trade.class, orphanRemoval = false)
    private Price price;

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
    }

    @Override
    public Optional<BigDecimal> getProfit() {
        BigDecimal marketValue = price.getClose().multiply(BigDecimal.valueOf(getSize()));
        Function<BigDecimal, Function<BigDecimal, Function<BigDecimal,Function<BigDecimal,BigDecimal>>>> paperProfit = ap -> pv -> mv -> c -> ap.add(mv).subtract(pv).subtract(c);
        return allPresent(paperProfit, super.getProfit().orElse(ZERO), getPurchaseValue(), marketValue, getCommission());
    }

    @Override
    public Long getWeightedHoldingTime() {
        return super.getWeightedHoldingTime() + calculateWeightedHoldingTime(getLast().toInstant(), getSize());
    }
}
