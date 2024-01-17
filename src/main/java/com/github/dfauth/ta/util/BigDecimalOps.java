package com.github.dfauth.ta.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.util.function.Function.identity;

public class BigDecimalOps {

    public static final BigDecimal HUNDRED = BigDecimal.valueOf(100.000);
    public static final BigDecimal ZERO3 = valueOf(ZERO);
    public static final BigDecimal ONE3 = valueOf(ONE);
    public static final BigDecimal ONE_CENT = valueOf(0.01d);

    public static boolean isGreaterThanZero(BigDecimal bd1) {
        return isGreaterThan(bd1, ZERO3);
    }
    public static boolean isGreaterThan(BigDecimal bd1, BigDecimal bd2) {
        return bd1.compareTo(bd2)>0;
    }

    public static boolean isGreaterThanOrEqualTo(BigDecimal bd1, BigDecimal bd2) {
        return bd1.compareTo(bd2)>=0;
    }

    public static boolean isLessThan(BigDecimal bd1, BigDecimal bd2) {
        return bd1.compareTo(bd2)<0;
    }

    public static boolean isLessThanOrEqualTo(BigDecimal bd1, BigDecimal bd2) {
        return bd1.compareTo(bd2)<=0;
    }

    public static BigDecimal divide(Integer t, Integer i) {
        return divide(valueOf(t), i);
    }

    public static BigDecimal divide(BigDecimal bd, Integer i) {
        return divide(bd, valueOf(i));
    }

    public static BigDecimal divide(BigDecimal bd1, BigDecimal bd2) {
        return bd1.divide(bd2, RoundingMode.HALF_UP);
    }

    public static BigDecimal pctChange(int i1, int i2) {
        return pctChange(i1, BigDecimal.valueOf(i2));
    }

    public static BigDecimal pctChange(int i, BigDecimal bd) {
        return pctChange(BigDecimal.valueOf(i),bd);
    }

    public static BigDecimal pctChange(BigDecimal bd1, BigDecimal bd2) {
        return scale(bd1.subtract(bd2),3).divide(scale(bd2,3),RoundingMode.HALF_UP);
    }

    public static BigDecimal scale(BigDecimal bd, int scale) {
        return bd.setScale(scale, RoundingMode.HALF_UP);
    }

    public static BigDecimal multiply(BigDecimal bd, Integer i) {
        return multiply(bd, BigDecimal.valueOf(i));
    }

    public static BigDecimal multiply(BigDecimal bd1, BigDecimal bd2) {
        return bd1.multiply(bd2);
    }

    public static BigDecimal compare(BigDecimal bd1, BigDecimal bd2, BinaryOperator<BigDecimal> f2) {
        return compare(bd1,bd2,identity(), f2);
    }
    public static <T> T compare(T t1, T t2, Function<T,BigDecimal> extractor, BinaryOperator<BigDecimal> f2) {
        BigDecimal bd1 = extractor.apply(t1);
        return f2.apply(bd1,extractor.apply(t2)) == bd1 ? t1 : t2;
    }

    public static BigDecimal valueOf(String v) {
        return valueOf(new BigDecimal(v));
    }

    public static BigDecimal valueOf(long v) {
        return valueOf(BigDecimal.valueOf(v));
    }

    public static BigDecimal valueOf(int v) {
        return valueOf(BigDecimal.valueOf(v));
    }

    public static BigDecimal valueOf(double v) {
        return valueOf(BigDecimal.valueOf(v));
    }

    public static BigDecimal valueOf(BigDecimal bd) {
        return valueOf(bd, 3);
    }

    public static BigDecimal valueOf(BigDecimal bd, int scale) {
        return bd.setScale(scale, RoundingMode.HALF_UP);
    }

    public static List<BigDecimal> collect(double... doubles) {
        return DoubleStream.of(doubles).mapToObj(BigDecimalOps::valueOf).collect(Collectors.toList());
    }

    public static BigDecimal add(BigDecimal bd1, BigDecimal bd2) {
        return valueOf(bd1.add(bd2));
    }

    public static BigDecimal subtract(BigDecimal bd1, BigDecimal bd2) {
        return valueOf(bd1.subtract(bd2));
    }
}
