package com.github.dfauth.ta.util;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.function.Function;

public class DateTimeUtils {

    private static DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static LocalDate toLocalDate(Timestamp date) {
        return toLocalDate(ZoneId.systemDefault()).apply(date.toInstant());
    }

    private static Function<Instant, LocalDate> toLocalDate(ZoneId z) {
        return i -> i.atZone(z).toLocalDate();
    }

    public static Timestamp toTimestamp(LocalDate date) {
        return toInstant().andThen(Instant::toEpochMilli).andThen(Timestamp::new).apply(date);
    }

    private static Function<LocalDate, Instant> toInstant() {
        return toInstant(LocalTime.MIDNIGHT, ZoneId.systemDefault());
    }

    private static Function<LocalDate, Instant> toInstant(LocalTime t, ZoneId z) {
        return d -> d.atTime(t).atZone(z).toInstant();
    }

    public enum Format {
        YYYYMMDD(str -> LocalDate.parse(str, yyyyMMdd), ld -> yyyyMMdd.format(ld));

        private Function<String, Temporal> parser;
        private Function<Temporal,String> formatter;

        Format(Function<String, Temporal> parser, Function<Temporal,String> formatter) {
            this.parser = parser;
            this.formatter = formatter;
        }

        public Temporal parse(String str) {
            return parser.apply(str);
        }

        public String format(Temporal temporal) {
            return formatter.apply(temporal);
        }
    }
}
