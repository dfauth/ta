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

    private String code;
    private BigDecimal max;
    private BigDecimal min;
    private BigDecimal current;

    public Drawdown(String code, BigDecimal bd) {
        this(code, bd, bd, bd);
    }

    public static Drawdown drawDownPrice(String code, List<PriceAction> prices) {
        return drawDown(code, prices, PriceAction::getClose);
    }

    public static Drawdown drawDown(String code, List<PriceAction> prices, Function<PriceAction, BigDecimal> f) {
        return drawDown(code, Lists.mapList(prices, f));
    }

    public static Drawdown drawDown(String code, List<BigDecimal> prices) {
        return drawDown(code, prices.stream());
    }

    public static Drawdown drawDown(String code, Stream<BigDecimal> prices) {
        return prices.reduce(new Drawdown(code, BigDecimal.ZERO), Drawdown::accumulate, Drawdown::combine);
    }

    private Drawdown combine(Drawdown drawdown) {
        throw new UnsupportedOperationException("Oops");
    }

    public Drawdown accumulate(BigDecimal bd) {
        if(max == null) {
            return new Drawdown(code, bd);
        }
        if(isGreaterThan(max, bd)) {
            if(isLessThan(min, bd)) {
                return new Drawdown(code, max, min, bd);
            } else {
                return new Drawdown(code, max, bd, bd);
            }
        } else {
            return new Drawdown(code, bd,min,bd);
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
