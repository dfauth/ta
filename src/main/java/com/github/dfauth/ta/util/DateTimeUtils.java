package com.github.dfauth.ta.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.function.Function;

public class DateTimeUtils {

    private static DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd");

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
