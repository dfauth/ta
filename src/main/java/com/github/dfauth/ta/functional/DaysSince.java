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
import java.util.List;
import java.util.Optional;

import static com.github.dfauth.ta.functional.Collectors.comparing;
import static com.github.dfauth.ta.functional.HistoricalOffset.zipWithHistoricalOffset;
import static com.github.dfauth.ta.functional.Lists.last;
import static com.github.dfauth.ta.functional.Lists.splitAt;
import static com.github.dfauth.ta.util.BigDecimalOps.compare;
import static java.util.stream.Collectors.toList;

public class DaysSince {

    public static Optional<Integer> lastHigh(List<BigDecimal> input) {
        return lastHigh(input, input.size());
    }
    public static Optional<Integer> lastHigh(List<BigDecimal> input, int period) {
        List<BigDecimal> last = splitAt(input, input.size() - period)._2();
        List<HistoricalOffset<BigDecimal>> zipped = zipWithHistoricalOffset(last).collect(toList());
        Optional<HistoricalOffset<BigDecimal>> l = last(zipped);
        Optional<HistoricalOffset<BigDecimal>> result = last(zipped.stream().collect(comparing((m, t) -> compare(m, t, HistoricalOffset::getPayload, BigDecimal::max))));
        return l.flatMap(ho -> result.map(ho::duration));
    }

    public static Optional<RecentHigh> recentHigh(List<Price> input) {
        return recentHigh(input, input.size());
    }

    public static Optional<RecentHigh> recentHigh(List<Price> input, int period) {
        List<Price> last = splitAt(input, input.size() - period)._2();
        List<HistoricalOffset<Price>> zipped = zipWithHistoricalOffset(last).collect(toList());
        Optional<HistoricalOffset<Price>> l = last(zipped);
        Optional<HistoricalOffset<Price>> result = last(zipped.stream().collect(comparing((m, t) -> compare(m, t, ho -> ho.getPayload().getClose(), BigDecimal::max))));
        return result.flatMap(r -> l.map(_l -> new RecentHigh(r,_l)));
    }

    @Data
    @AllArgsConstructor
    public static class RecentHigh {
        @JsonIgnore
        private HistoricalOffset<Price> high;
        @JsonIgnore
        private HistoricalOffset<Price> last;

        public int getDaysSince() {
            return last.duration(high);
        }

        public BigDecimal getPrice() {
            return high.getPayload().get_close();
        }

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        @JsonSerialize(using= LocalDateSerializer.class)
        @JsonDeserialize(using= LocalDateDeserializer.class)
        public LocalDate getDate() {
            return high.getPayload().getDate();
        }

        public BigDecimal getPctBelow() {
            return last.getPayload().get_close().subtract(getPrice()).divide(getPrice(), RoundingMode.HALF_UP);
        }
    }
}
