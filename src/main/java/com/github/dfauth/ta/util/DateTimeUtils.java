package com.github.dfauth.ta.util;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.Locale;
import java.util.function.Function;

public class DateTimeUtils {

    private static DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd");
    public static DateTimeFormatter spreadsheetDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
//            new DateTimeFormatterBuilder()
//                    .parseCaseInsensitive()
//                    .appendValue(YEAR, 4)
//                    .appendLiteral('-')
//                    .appendValue(MONTH_OF_YEAR, 2)
//                    .appendLiteral('-')
//                    .appendValue(DAY_OF_MONTH, 2)
//                    .appendLiteral("T")
//                    .appendValue(HOUR_OF_DAY, 2)
//                    .appendLiteral(':')
//                    .appendValue(MINUTE_OF_HOUR, 2)
//                    .appendLiteral(':')
//                    .appendValue(SECOND_OF_MINUTE, 2)
//                    .appendLiteral('.')
//                    .appendValue(ChronoField.MILLI_OF_SECOND, 3)
//                    .appendLiteral("Z")
//                    .toFormatter();

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
