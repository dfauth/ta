package com.github.dfauth.ta.functions;

import com.github.dfauth.ta.functional.Lists;
import com.github.dfauth.ta.model.PriceAction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.dfauth.ta.util.BigDecimalOps.*;

@Slf4j
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Drawdown {

    private BigDecimal max;
    private BigDecimal min;
    private BigDecimal current;

    public Drawdown(BigDecimal bd) {
        this(bd, bd, bd);
    }

    public static Drawdown drawDownPrice(List<PriceAction> prices) {
        return drawDown(prices, PriceAction::getClose);
    }

    public static Drawdown drawDown(List<PriceAction> prices, Function<PriceAction, BigDecimal> f) {
        return drawDown(Lists.mapList(prices, f));
    }

    public static Drawdown drawDown(List<BigDecimal> prices) {
        return drawDown(prices.stream());
    }

    public static Drawdown drawDown(Stream<BigDecimal> prices) {
        return prices.reduce(new Drawdown(), Drawdown::accumulate, Drawdown::combine);
    }

    private Drawdown combine(Drawdown drawdown) {
        throw new UnsupportedOperationException("Oops");
    }

    public Drawdown accumulate(BigDecimal bd) {
        if(max == null) {
            return new Drawdown(bd);
        }
        if(isGreaterThan(max, bd)) {
            if(isLessThan(min, bd)) {
                return new Drawdown(max, min, bd);
            } else {
                return new Drawdown(max, bd, bd);
            }
        } else {
            return new Drawdown(bd,min,bd);
        }
    }

    public BigDecimal getMaxDrawDown() {
        return Optional.ofNullable(min).map(m ->
                divideWithZeroCheck(m.subtract(max), max)
                        .orElse(ZERO3)).orElse(ZERO3);
    }

    public BigDecimal getDrawDown() {
        return Optional.ofNullable(current).map(c ->
                divideWithZeroCheck(c.subtract(max), max)
                        .orElse(ZERO3)).orElse(ZERO3);
    }

}
