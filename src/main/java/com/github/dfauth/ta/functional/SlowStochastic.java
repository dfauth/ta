package com.github.dfauth.ta.functional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dfauth.ta.model.Price;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

import static com.github.dfauth.ta.util.BigDecimalOps.HUNDRED;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class SlowStochastic {

    @JsonIgnore private Price last;
    @JsonIgnore private BigDecimal maxHigh;
    @JsonIgnore private BigDecimal minLow;

    public static Stochastic stochasticOscillator(List<Price> prices, int fast, int slow) {
        return prices.stream().collect(new StochasticReducer(fast, slow));
    }

    public SlowStochastic merge(SlowStochastic s2) {
        return new SlowStochastic(
                last.isAfter(s2.last) ? last : s2.last,
                maxHigh.max(s2.maxHigh),
                minLow.min(s2.minLow)
        );
    }

    public SlowStochastic merge(Price price) {
        return Optional.ofNullable(last)
                .map(ignored -> new SlowStochastic(
                    price,
                    maxHigh.max(price.getHigh()),
                    minLow.min(price.getLow()))
                )
                .orElseGet(() -> new SlowStochastic(price, price.getHigh(), price.getLow()));
    }

    public BigDecimal getK() {
        return calculateK(last.getClose(), maxHigh, minLow);
    }

    public static BigDecimal calculateK(BigDecimal close, BigDecimal maxHigh, BigDecimal minLow) {
        return HUNDRED.multiply(close.subtract(minLow).divide(maxHigh.subtract(minLow), RoundingMode.HALF_UP));
    }

}
