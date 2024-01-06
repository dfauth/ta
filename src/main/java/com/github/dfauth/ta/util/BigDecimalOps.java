package com.github.dfauth.ta.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import static java.math.BigDecimal.valueOf;
import static java.util.function.Function.identity;

public class BigDecimalOps {

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
        return bd.multiply(BigDecimal.valueOf(i));
    }

    public static BigDecimal compare(BigDecimal bd1, BigDecimal bd2, BinaryOperator<BigDecimal> f2) {
        return compare(bd1,bd2,identity(), f2);
    }
    public static <T> T compare(T t1, T t2, Function<T,BigDecimal> extractor, BinaryOperator<BigDecimal> f2) {
        BigDecimal bd1 = extractor.apply(t1);
        return f2.apply(bd1,extractor.apply(t2)) == bd1 ? t1 : t2;
    }
}
