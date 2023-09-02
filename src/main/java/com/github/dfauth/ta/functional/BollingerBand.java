package com.github.dfauth.ta.functional;

import java.math.BigDecimal;
import java.util.List;

import static com.github.dfauth.ta.functional.Tuple3.tuple3;

public class BollingerBand {

    public static Tuple3<BigDecimal, BigDecimal, BigDecimal> calculateBollingerBands(List<BigDecimal> priceList, int period, BigDecimal stdDevMultiplier) {
        if (priceList == null || priceList.isEmpty() || period <= 0 || stdDevMultiplier.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid input parameters");
        }

        if (priceList.size() < period) {
            throw new IllegalArgumentException("Insufficient data for the given period");
        }

        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal sumSquared = BigDecimal.ZERO;
        BigDecimal midPoint = BigDecimal.ZERO;

        for (int i = 0; i < period; i++) {
            BigDecimal price = priceList.get(i);
            sum = sum.add(price);
            sumSquared = sumSquared.add(price.multiply(price));
        }

        BigDecimal highValue = BigDecimal.ZERO;
        BigDecimal lowValue = BigDecimal.ZERO;

        for (int i = period; i < priceList.size(); i++) {
            BigDecimal price = priceList.get(i);
            sum = sum.add(price).subtract(priceList.get(i - period));
            sumSquared = sumSquared.add(price.multiply(price)).subtract(priceList.get(i - period).multiply(priceList.get(i - period)));

            midPoint = sum.divide(BigDecimal.valueOf(period), BigDecimal.ROUND_HALF_UP);

            BigDecimal variance = sumSquared.divide(BigDecimal.valueOf(period), BigDecimal.ROUND_HALF_UP)
                    .subtract(midPoint.multiply(midPoint));

            BigDecimal stdDeviation = BigDecimal.valueOf(Math.sqrt(variance.doubleValue()));

            highValue = midPoint.add(stdDevMultiplier.multiply(stdDeviation));
            lowValue = midPoint.subtract(stdDevMultiplier.multiply(stdDeviation));
        }

        return tuple3(highValue, midPoint, lowValue);
    }
}
