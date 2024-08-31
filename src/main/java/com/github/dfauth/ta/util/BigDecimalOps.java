package com.github.dfauth.ta.util;

import io.github.dfauth.trycatch.Try;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import static io.github.dfauth.trycatch.Try.tryWith;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.util.function.Predicate.not;

public interface BigDecimalOps extends UnaryOperator<BigDecimal> {

    BigDecimal HUNDRED = BigDecimal.valueOf(100.000);
    BigDecimal ZERO3 = valueOf(ZERO);
    BigDecimal ONE3 = valueOf(ONE);
    BigDecimal ONE_CENT = valueOf(0.01d);

    static boolean isGreaterThanZero(BigDecimal bd1) {
        return isGreaterThan(bd1, ZERO3);
    }
    static boolean isGreaterThan(BigDecimal bd1, BigDecimal bd2) {
        return bd1.compareTo(bd2)>0;
    }

    static boolean isGreaterThanOrEqualTo(BigDecimal bd1, BigDecimal bd2) {
        return bd1.compareTo(bd2)>=0;
    }

    static boolean isLessThanZero(BigDecimal bd) {
        return isLessThan(bd, ZERO3);
    }

    static boolean isLessThan(BigDecimal bd1, BigDecimal bd2) {
        return bd1.compareTo(bd2)<0;
    }

    static boolean isLessThanOrEqualTo(BigDecimal bd1, BigDecimal bd2) {
        return bd1.compareTo(bd2)<=0;
    }

    static BigDecimal divide(Integer t, Integer i) {
        return divide(valueOf(t), i);
    }

    static BigDecimal divide(BigDecimal bd, Integer i) {
        return divide(bd, valueOf(i));
    }

    static BigDecimalOps divide(BigDecimal bd1) {
        return bd -> divide(bd1,bd);
    }

    static BigDecimal divide(BigDecimal bd1, BigDecimal bd2) {
        return bd1.divide(bd2, RoundingMode.HALF_UP);
    }

    static Optional<BigDecimal> divideWithZeroCheck(BigDecimal bd1, BigDecimal bd2) {
        return Optional.ofNullable(bd2).filter(not(ZERO3::equals)).map(_bd2 -> bd1.divide(_bd2, RoundingMode.HALF_UP));
    }

    static Optional<BigDecimal> optDivide(BigDecimal bd1, BigDecimal bd2) {
        return tryDivide(bd1,bd2).toOptional();
    }

    static Try<BigDecimal> tryDivide(BigDecimal bd1, BigDecimal bd2) {
        return tryWith(() -> divide(bd1, bd2));
    }

    static Optional<BigDecimal> pctChange(int i1, int i2) {
        return pctChange(i1, BigDecimal.valueOf(i2));
    }

    static Optional<BigDecimal> pctChange(int i, BigDecimal bd) {
        return pctChange(BigDecimal.valueOf(i),bd);
    }

    static Optional<BigDecimal> pctChange(BigDecimal bd1, BigDecimal bd2) {
        return divideWithZeroCheck(scale(bd1.subtract(bd2),3),scale(bd2,3));
    }

    static BigDecimal pctChangeOrZero(BigDecimal bd1, BigDecimal bd2) {
        return divideWithZeroCheck(scale(bd1.subtract(bd2),3),scale(bd2,3)).orElse(ZERO);
    }

    static BigDecimal scale(BigDecimal bd, int scale) {
        return bd.setScale(scale, RoundingMode.HALF_UP);
    }

    static BigDecimal multiply(BigDecimal bd, Integer i) {
        return multiply(bd, BigDecimal.valueOf(i));
    }

    static BigDecimal multiply(BigDecimal bd1, BigDecimal bd2) {
        return bd1.multiply(bd2);
    }

    static BigDecimal compare(BigDecimal bd1, BigDecimal bd2, BinaryOperator<BigDecimal> f2) {
        return compare(bd1,bd2,t -> t, f2);
    }
    static <T> T compare(T t1, T t2, Function<T, BigDecimal> extractor, BinaryOperator<BigDecimal> f2) {
        BigDecimal bd1 = extractor.apply(t1);
        return f2.apply(bd1,extractor.apply(t2)) == bd1 ? t1 : t2;
    }

    static BigDecimal valueOf(String v) {
        return valueOf(new BigDecimal(v));
    }

    static BigDecimal valueOf(long v) {
        return valueOf(BigDecimal.valueOf(v));
    }

    static BigDecimal valueOf(int v) {
        return valueOf(BigDecimal.valueOf(v));
    }

    static BigDecimal valueOf(double v) {
        return valueOf(BigDecimal.valueOf(v));
    }

    static BigDecimal valueOf(BigDecimal bd) {
        return valueOf(bd, 3);
    }

    static BigDecimal valueOf(BigDecimal bd, int scale) {
        return bd.setScale(scale, RoundingMode.HALF_UP);
    }

    static List<BigDecimal> collect(double... doubles) {
        return collect(BigDecimalOps::valueOf, doubles);
    }

    static <T> List<T> collect(Function<Double, T> f, double... doubles) {
        return DoubleStream.of(doubles).boxed().map(f).collect(Collectors.toList());
    }

    static BigDecimal add(BigDecimal bd1, BigDecimal bd2) {
        return valueOf(bd1.add(bd2));
    }

    static BigDecimal subtract(BigDecimal bd1, BigDecimal bd2) {
        return valueOf(bd1.subtract(bd2));
    }

    static Optional<BigDecimal> maxOf(BigDecimal... bds) {
        return Stream.of(bds).reduce(BigDecimal::max);
    }

    default BigDecimal by(BigDecimal divisor) {
        return apply(divisor);
    }
}
