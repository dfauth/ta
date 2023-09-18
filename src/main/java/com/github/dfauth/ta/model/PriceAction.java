package com.github.dfauth.ta.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.UnaryOperator;

public interface PriceAction {

    BigDecimal getOpen();
    BigDecimal getHigh();
    BigDecimal getLow();
    BigDecimal getClose();
    int getVolume();
    default BigDecimal getTrueRange(BigDecimal previousClose) {
        return getRange()
                .max(getHigh().subtract(previousClose).abs()
                        .max(getLow().subtract(previousClose).abs()
                        )
                );
    }

    default BigDecimal getRange() {
        return getHigh().subtract(getLow());
    }

    default boolean isRising() {
        return getClose().compareTo(getOpen()) > 0;
    }

    default boolean isFalling() {
        return getClose().compareTo(getOpen()) < 0;
    }

    default PriceAction map(UnaryOperator<BigDecimal> f) {
        return new PriceAction() {
            @Override
            public BigDecimal getOpen() {
                return f.apply(getOpen());
            }

            @Override
            public BigDecimal getHigh() {
                return f.apply(getHigh());
            }

            @Override
            public BigDecimal getLow() {
                return f.apply(getLow());
            }

            @Override
            public BigDecimal getClose() {
                return f.apply(getClose());
            }

            @Override
            public int getVolume() {
                return f.apply(BigDecimal.valueOf(getVolume())).intValue();
            }
        };
    }

    static UnaryOperator<BigDecimal> divide(int period) {
        return bd -> bd.divide(BigDecimal.valueOf(period), RoundingMode.HALF_UP);
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
