package com.github.dfauth.ta.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.function.Function;

import static com.github.dfauth.ta.model.Side.Buy;
import static com.github.dfauth.ta.model.Side.Sell;
import static java.util.function.Function.identity;

@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Getter
public class Sided<T extends Number> {

    private final Side side;
    private final T value;

    public T getSignedValue() {
        return toType(getBigDecimal());
    }

    public BigDecimal getBigDecimal() {
        return BigDecimal.valueOf(side.getMultiplier() * value.doubleValue());
    }

    public T toType(BigDecimal bd) {
        if(value instanceof Integer) {
            return (T) Integer.valueOf(bd.intValue());
        } else if( value instanceof Double) {
            return (T) Double.valueOf(bd.doubleValue());
        } else if( value instanceof Long) {
            return (T) Double.valueOf(bd.doubleValue());
        } else if( value instanceof Float) {
            return (T) Double.valueOf(bd.doubleValue());
        } else if( value instanceof Short) {
            return (T) Double.valueOf(bd.doubleValue());
        } else if( value instanceof BigDecimal) {
            return (T) bd;
        } else if( value instanceof BigInteger) {
            return (T) bd;
        }
        throw new IllegalArgumentException("Unknown or unsupported type: "+value);
    }

    public static <T extends Number> Sided<T> sided(Side side, T t) {
        return new Sided<>(side, t);
    }

    public <R extends Number> Sided<? extends Number> add(Sided<T> other) {
        return fromSignedValue(getBigDecimal().add(other.getBigDecimal()))
                .map(typeValue(value, other.value));
    }

    public <R extends Number> Sided<? extends Number> subtract(Sided<R> other) {
        return fromSignedValue(getBigDecimal().subtract(other.getBigDecimal()))
                .map(typeValue(value, other.value));
    }

    public <R extends Number> Sided<? extends Number> multiply(Sided<R> other) {
        return fromSignedValue(getBigDecimal().multiply(other.getBigDecimal()))
                .map(typeValue(value, other.value));
    }

    public <R extends Number> Sided<? extends Number> divide(Sided<R> other) {
        return fromSignedValue(getBigDecimal().divide(other.getBigDecimal(), RoundingMode.HALF_UP))
                .map(typeValue(value, other.value));
    }

    private static <L extends Number, R extends Number> Function<BigDecimal, ? extends Number> typeValue(L left, R right) {
        if(left instanceof Double && right instanceof Double) {
            return BigDecimal::doubleValue;
        } else if(left instanceof Integer && right instanceof Integer) {
            return BigDecimal::intValue;
        } else if(left instanceof Long && right instanceof Long) {
            return BigDecimal::longValue;
        } else if(left instanceof Float && right instanceof Float) {
            return BigDecimal::floatValue;
        } else if(left instanceof Short && right instanceof Short) {
            return BigDecimal::shortValue;
        } else {
            return identity();
        }
    }

    public <R extends Number> Sided<R> map(Function<T,R> f) {
        return sided(side, f.apply(value));
    }

    public static Sided<BigDecimal> fromSignedValue(BigDecimal bd) {
        return bd.doubleValue() >= 0 ? sided(Buy,bd) : sided(Sell, bd.abs());
    }
}
