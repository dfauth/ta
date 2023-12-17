package com.github.dfauth.ta.functional;

import com.github.dfauth.ta.model.PriceAction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static java.math.BigDecimal.ZERO;

@Slf4j
public class VWAP {

    public static Function<List<PriceAction>, Optional<VolumeWeightedPrice>> calculate(int period) {
        return prices -> calculate(prices, period);
    }

    public static Optional<VolumeWeightedPrice> calculate(List<PriceAction> prices, int period) {
        if (prices == null || prices.isEmpty() || period <= 0) {
//            throw new IllegalArgumentException("Invalid input parameters");
            log.warn("Invalid input parameters {}, {}",period);
            return Optional.empty();
        }

        if (prices.size() < period) {
//            throw new IllegalArgumentException("Insufficient data for the given period");
            log.warn("Insufficient data {} for the given period {}",prices.size(),period);
            return Optional.empty();
        }

        return Optional.ofNullable(prices.stream().map(VolumeWeightedPrice::new).reduce(new VolumeWeightedPrice(), VolumeWeightedPrice::add, VolumeWeightedPrice::add));
    }

    @Data
    @AllArgsConstructor
    public static class VolumeWeightedPrice {

        private final BigDecimal vp;
        private final int vol;

        public VolumeWeightedPrice() {
            this(ZERO, 0);
        }

        public VolumeWeightedPrice(PriceAction pa) {
            this(pa.getClose().multiply(BigDecimal.valueOf(pa.getVolume())), pa.getVolume());
        }

        public VolumeWeightedPrice add(VolumeWeightedPrice vwap) {
            return new VolumeWeightedPrice(vwap.getVp().add(getVp()), vwap.getVol()+ getVol());
        }

        public BigDecimal getVwap() {
            return getVp().divide(BigDecimal.valueOf(getVol()), RoundingMode.HALF_UP);
        }
    }
}
