package com.github.dfauth.ta.model;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.dfauth.ta.util.DateTimeUtils.Format.YYYYMMDD;

@Slf4j
public enum PositionDatePredicate implements Function<String, Predicate<Position>> {

    START_BEFORE((p, ld) -> p.getOpen().isBefore(ld)),
    START_AFTER((p, ld) -> p.getOpen().isAfter(ld)),
    END_BEFORE((p, ld) -> p.getLast().isBefore(ld)),
    END_AFTER((p, ld) -> p.getLast().isAfter(ld));
    private BiPredicate<Position, LocalDate> p2;

    public static Predicate<Position> ignore() {
        return p -> true;
    }

    PositionDatePredicate(BiPredicate<Position, LocalDate> p2) {
        this.p2 = p2;
    }

    @Override
    public Predicate<Position> apply(String localDate) {
        return p -> p2.test(p, LocalDate.from(YYYYMMDD.parse(localDate)));
    }
}
