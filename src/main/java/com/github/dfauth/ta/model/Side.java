package com.github.dfauth.ta.model;

import lombok.Getter;

import java.util.function.UnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Getter
public enum Side {
    Sell, Buy, Int, Div;

    public int getMultiplier() {
        return IntStream.range(0,values().length).filter(i -> values()[i] == this).map(i -> (int)(2.0d*((double)i-0.5d))).findFirst().orElseThrow();
    }

    public static Side fromMultiplier(int multiplier) {
        return Stream.of(values()).filter(s -> s.getMultiplier() == multiplier).findFirst().orElseThrow();
    }

    public static Side fromString(Object o) {
        if(o instanceof String) {
            return Stream.of(Side.values()).filter(v -> hazyEquals(v.name(), (String)o)).findFirst().orElse(Buy);
        } else if(o instanceof Integer) {
            return fromMultiplier((Integer)o);
        } else {
            throw new IllegalArgumentException("Unexpected type: "+o);
        }
    }

    private static boolean hazyEquals(String l, String r) {
        return l.equalsIgnoreCase(r) || Character.toUpperCase(l.toCharArray()[0]) == Character.toUpperCase(r.toCharArray()[0]);
    }

    public boolean isSell() {
        return !isBuy();
    }

    public boolean isBuy() {
        return this == Buy;
    }

    public Side flip() {
        return isBuy() ? Sell : Buy;
    }

    public <T extends Number> Sided<T> sided(T t) {
        return Sided.sided(this, t);
    }

    public Side map(UnaryOperator<Side> f) {
        return f.apply(this);
    }

    public boolean isInt() {
        return this == Int;
    }

    public boolean isDiv() {
        return this == Div;
    }
}
