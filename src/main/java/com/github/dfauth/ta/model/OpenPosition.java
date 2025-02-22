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
    public BigDecimal getProfit() {
        var marketValue = price.getClose().multiply(BigDecimal.valueOf(getSize()));
        Function<BigDecimal, Function<BigDecimal, Function<BigDecimal,BigDecimal>>> paperProfit = pv -> mv -> c -> mv.subtract(pv).subtract(c);
        return Optional.ofNullable(super.getProfit()).map(p -> p.add(marketValue))
                .orElse(allPresent(paperProfit, getPurchaseValue(), marketValue, getCommission()).orElse(null));
//                .orElse(Optional.ofNullable(getPurchaseValue()).map(pv -> marketValue.subtract(pv).subtract(getCommission())).orElse(null));
    }

    @Override
    public Long getWeightedHoldingTime() {
        return super.getWeightedHoldingTime() + calculateWeightedHoldingTime(getLast().toInstant(), getSize());
    }
}
