package com.github.dfauth.ta.model;

import java.util.function.Predicate;

public enum Theme implements Predicate<Position> {

    MOMENTUM,
    MEAN_REVERSION,
    COMPOUNDER,
    DIVIDEND;

    @Override
    public boolean test(Position position) {
        return position.getTrades().stream().map(Trade::getTheme).allMatch(this::equals);
    }
}
