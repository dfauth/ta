package com.github.dfauth.ta.functional;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.github.dfauth.ta.model.Price;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import static com.github.dfauth.ta.functional.Tuple2.tuple2;

public class DaysSince {

    public static Optional<Integer> lastHigh(List<BigDecimal> input) {
        Iterator<BigDecimal> it = input.iterator();
        AtomicReference<Tuple2<Integer, BigDecimal>> max = new AtomicReference<>(null);
        IntStream.range(0,input.size())
                .mapToObj(i -> tuple2(input.size()-1-i, it.next()))
                .filter(t2 -> Optional.ofNullable(max.get()).map(_m -> _m._2().compareTo(t2._2()) < 0).orElse(true))
                .forEach(max::set);
        return Optional.ofNullable(max.get()).map(Tuple2::_1);
    }

    public static Optional<RecentHigh> recentHigh(List<Price> input) {
        Iterator<Price> it = input.iterator();
        AtomicReference<Tuple2<Integer, Price>> max = new AtomicReference<>(null);
        IntStream.range(0,input.size())
                .mapToObj(i -> tuple2(input.size()-1-i, it.next()))
                .filter(t2 -> Optional.ofNullable(max.get()).map(_m -> _m._2().get_close().compareTo(t2._2().get_close()) < 0).orElse(true))
                .forEach(max::set);
        return Optional.ofNullable(max.get()).flatMap(t2 -> Lists.last(input).map(latest -> new RecentHigh(t2, tuple2(0,latest))));
    }

    @Data
    @AllArgsConstructor
    public static class RecentHigh {
        @JsonIgnore
        private Tuple2<Integer, Price> high;
        @JsonIgnore
        private Tuple2<Integer, Price> last;

        public int getDaysSince() {
            return last._1() - high._1();
        }

        public BigDecimal getPrice() {
            return high._2().get_close();
        }

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        @JsonSerialize(using= LocalDateSerializer.class)
        @JsonDeserialize(using= LocalDateDeserializer.class)
        public LocalDate getDate() {
            return high._2().getDate();
        }

        public BigDecimal getPctBelow() {
            return last._2().get_close().subtract(getPrice()).divide(getPrice(), RoundingMode.HALF_UP);
        }
    }
}
