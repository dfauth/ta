package com.github.dfauth.ta.functional;

import com.github.dfauth.ta.model.PriceAction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static java.lang.Math.abs;

@Slf4j
public class BollingerBand {

    public static Function<List<PriceAction>, List<BBPoint>> calculateBollingerBands(int period, double stdDevMultiplier) {
        return prices -> calculateBollingerBands(Lists.mapList(prices, PriceAction::getClose), period, stdDevMultiplier);
    }

    public static List<BBPoint> calculateBollingerBands(List<BigDecimal> priceList, int period, double stdDevMultiplier) {
        if (priceList == null || priceList.isEmpty() || period <= 0 || stdDevMultiplier <= 0) {
//            throw new IllegalArgumentException("Invalid input parameters");
            log.warn("Invalid input parameters {}, {}",period, stdDevMultiplier);
            return Collections.emptyList();
        }

        if (priceList.size() < period) {
//            throw new IllegalArgumentException("Insufficient data for the given period");
            log.warn("Insufficient data {} for the given period {}",priceList.size(),period);
            return Collections.emptyList();
        }

        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal sumSquared = BigDecimal.ZERO;
        BigDecimal midPoint = BigDecimal.ZERO;

        List<BBPoint> tmp = new ArrayList<>();
        for (int i = 0; i < period; i++) {
            BigDecimal price = priceList.get(i);
            sum = sum.add(price);
            sumSquared = sumSquared.add(price.multiply(price));
        }

        BigDecimal highValue = BigDecimal.ZERO;
        BigDecimal lowValue = BigDecimal.ZERO;
        BigDecimal bdStdMultiplier = BigDecimal.valueOf(stdDevMultiplier);

        for (int i = period; i < priceList.size(); i++) {
            BigDecimal price = priceList.get(i);
            sum = sum.add(price).subtract(priceList.get(i - period));
            sumSquared = sumSquared.add(price.multiply(price)).subtract(priceList.get(i - period).multiply(priceList.get(i - period)));

            midPoint = sum.divide(BigDecimal.valueOf(period), BigDecimal.ROUND_HALF_UP);

            BigDecimal variance = sumSquared.divide(BigDecimal.valueOf(period), BigDecimal.ROUND_HALF_UP)
                    .subtract(midPoint.multiply(midPoint));

            BigDecimal stdDeviation = BigDecimal.valueOf(Math.sqrt(abs(variance.doubleValue())));

            highValue = midPoint.add(bdStdMultiplier.multiply(stdDeviation));
            lowValue = midPoint.subtract(bdStdMultiplier.multiply(stdDeviation));
            tmp.add(new BBPoint(highValue, midPoint, lowValue, price));
        }
        return tmp;
    }

    @Data
    @AllArgsConstructor
    public static class BBPoint {
        private final BigDecimal highValue;
        private final BigDecimal midPoint;
        private final BigDecimal lowValue;
        private final BigDecimal price;

        public BigDecimal getHighMargin() {
            return highValue.subtract(price);
        }

        public BigDecimal getHighMarginPct() {
            return getHighMargin().divide(price, RoundingMode.HALF_UP);
        }

        public BigDecimal getLowMargin() {
            return price.subtract(lowValue);
        }

        public BigDecimal getLowMarginPct() {
            return getLowMargin().divide(price, RoundingMode.HALF_UP);
        }

        public BigDecimal getMinMarginPct() {
            return getLowMarginPct().min(getHighMarginPct());
        }

    }
}
