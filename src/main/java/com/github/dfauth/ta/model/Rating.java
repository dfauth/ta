package com.github.dfauth.ta.model;

import java.util.stream.Stream;

public enum Rating {

    STRONG_BUY("Strong Buy"),
    BUY("Buy"),
    HOLD("Hold"),
    SELL("Sell"),
    STRONG_SELL("Strong Sell");

    private final String label;

    Rating(String label) {
        this.label = label;
    }

    public static Rating fromString(String s) {
        return Stream.of(values()).filter(v -> v.label.equals(s)).findFirst().orElse(null);
    }
}
