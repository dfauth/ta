package com.github.dfauth.ta.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public interface PriceAction {

    BigDecimal getOpen();
    BigDecimal getHigh();
    BigDecimal getLow();
    BigDecimal getClose();
    int getVolume();
    @JsonIgnore
    default BigDecimal getTrueRange(PriceAction previous) {
        return trueRange.apply(previous, this);
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

    default PriceAction mapPrices(UnaryOperator<BigDecimal> f) {
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
        return mapPrices(divisionOperator(n));
    }

    static UnaryOperator<BigDecimal> divisionOperator(int period) {
        return divisionOperator(period, 6);
    }

    static UnaryOperator<BigDecimal> divisionOperator(int period, int scale) {
        return bd -> bd.divide(BigDecimal.valueOf(period).setScale(scale), scale, RoundingMode.HALF_UP);
    }

    default PriceAction add(PriceAction pa) {
        return add(this,pa);
    }

    default PriceAction subtract(PriceAction pa) {
        return subtract(this,pa);
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

    static PriceAction subtract(PriceAction pa1, PriceAction pa2) {
        return new PriceAction() {
            @Override
            public BigDecimal getOpen() {
                return pa1.getOpen().subtract(pa2.getOpen());
            }

            @Override
            public BigDecimal getHigh() {
                return pa1.getHigh().subtract(pa2.getHigh());
            }

            @Override
            public BigDecimal getLow() {
                return pa1.getLow().subtract(pa2.getLow());
            }

            @Override
            public BigDecimal getClose() {
                return pa1.getClose().subtract(pa2.getClose());
            }

            @Override
            public int getVolume() {
                return pa1.getVolume() - pa2.getVolume();
            }
        };
    }

    BiFunction<PriceAction,PriceAction,BigDecimal> trueRange = (previous, current) -> current.getRange()
            .max(current.getHigh().subtract(previous.getClose()).abs()
                    .max(current.getLow().subtract(previous.getClose()).abs()
                    )
            );

    PriceAction ZERO = new PriceAction() {
        @Override
        public BigDecimal getOpen() {
            return BigDecimal.ZERO;
        }

        @Override
        public BigDecimal getHigh() {
            return BigDecimal.ZERO;
        }

        @Override
        public BigDecimal getLow() {
            return BigDecimal.ZERO;
        }

        @Override
        public BigDecimal getClose() {
            return BigDecimal.ZERO;
        }

        @Override
        public int getVolume() {
            return 0;
        }
    };

    Function<List<PriceAction>, Optional<PriceAction>> SMA = priceActions -> priceActions.stream().reduce((pa1, pa2) -> pa1.add(pa2)).map(ps -> ps.divide(priceActions.size()));


}
