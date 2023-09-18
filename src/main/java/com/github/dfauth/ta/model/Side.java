package com.github.dfauth.ta.model;

import java.util.stream.Stream;

public enum Side {
    Buy, Sell;

    public static Side fromString(Object o) {
        if(o instanceof String) {
            return Stream.of(Side.values()).filter(v -> v.name().equalsIgnoreCase((String)o)).findFirst().orElse(Buy);
        } else if(o instanceof Integer) {
            return Side.values()[(Integer)o];
        } else {
            throw new IllegalArgumentException("Unexpected type: "+o);
        }
    }
}
