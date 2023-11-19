package com.github.dfauth.ta.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.UnaryOperator;

public interface PriceAction {

    BigDecimal getOpen();
    BigDecimal getHigh();
    BigDecimal getLow();
    BigDecimal getClose();
    int getVolume();
    @JsonIgnore
    default BigDecimal getTrueRange(BigDecimal previousClose) {
        return getRange()
                .max(getHigh().subtract(previousClose).abs()
                        .max(getLow().subtract(previousClose).abs()
                        )
                );
    }

    @JsonIgnore
    default BigDecimal getRange() {
        return getHigh().subtract(getLow());
    }

    @JsonIgnore
    default boolean isRising() {
        return getClose().compareTo(getOpen()) > 0;
    }

    @JsonIgnore
    default boolean isFalling() {
        return getClose().compareTo(getOpen()) < 0;
    }

    default PriceAction map(UnaryOperator<BigDecimal> f) {
        return new PriceAction() {
            @Override
            public BigDecimal getOpen() {
                return f.apply(PriceAction.this.getOpen());
            }

            @Override
            public BigDecimal getHigh() {
                return f.apply(PriceAction.this.getHigh());
            }

            @Override
            public BigDecimal getLow() {
                return f.apply(PriceAction.this.getLow());
            }

            @Override
            public BigDecimal getClose() {
                return f.apply(PriceAction.this.getClose());
            }

            @Override
            public int getVolume() {
                return f.apply(BigDecimal.valueOf(PriceAction.this.getVolume())).intValue();
            }
        };
    }

    default PriceAction divide(int n) {
        return map(divisionOperator(n));
    }

    static UnaryOperator<BigDecimal> divisionOperator(int period) {
        return bd -> bd.divide(BigDecimal.valueOf(period), RoundingMode.HALF_UP);
    }

    default PriceAction add(PriceAction pa) {
        return add(this,pa);
    }

    static PriceAction add(PriceAction pa1, PriceAction pa2) {
        return new PriceAction() {
            @Override
            public BigDecimal getOpen() {
                return pa1.getOpen().add(pa2.getOpen());
            }

            @Override
            public BigDecimal getHigh() {
                return pa1.getHigh().add(pa2.getHigh());
            }

            @Override
            public BigDecimal getLow() {
                return pa1.getLow().add(pa2.getLow());
            }

            @Override
            public BigDecimal getClose() {
                return pa1.getClose().add(pa2.getClose());
            }

            @Override
            public int getVolume() {
                return pa1.getVolume() + pa2.getVolume();
            }
        };
    }
}
