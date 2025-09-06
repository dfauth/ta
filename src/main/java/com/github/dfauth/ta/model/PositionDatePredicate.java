package com.github.dfauth.ta.model;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.dfauth.ta.util.DateTimeUtils.Format.YYYYMMDD;

@Slf4j
public enum PositionDatePredicate implements Function<String, Predicate<Position>> {

    START_DATE((p, ld) -> p.getDate().toLocalDateTime().toLocalDate().isAfter(ld)),
    END_DATE((p, ld) -> p.getLast().toLocalDateTime().toLocalDate().isBefore(ld));
    private BiPredicate<Position, LocalDate> p2;

    PositionDatePredicate(BiPredicate<Position, LocalDate> p2) {
        this.p2 = p2;
    }

    @Override
    public Predicate<Position> apply(String localDate) {
        return p -> p2.test(p, LocalDate.from(YYYYMMDD.parse(localDate)));
    }
}
