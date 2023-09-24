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
import java.util.concurrent.atomic.AtomicReference;

import static com.github.dfauth.ta.functional.HistoricalOffset.zipWithHistoricalOffset;

public class DaysSince {

    public static Optional<Integer> lastHigh(List<BigDecimal> input) {
        AtomicReference<HistoricalOffset<BigDecimal>> max = new AtomicReference<>(null);
        zipWithHistoricalOffset(input)
                .filter(ho -> Optional.ofNullable(max.get()).map(_m -> _m.getPayload().compareTo(ho.getPayload()) < 0).orElse(true))
                .forEach(max::set);
        return Optional.ofNullable(max.get()).map(HistoricalOffset::getOffset);
    }

    public static Optional<RecentHigh> recentHigh(List<Price> input) {
        AtomicReference<HistoricalOffset<Price>> max = new AtomicReference<>(null);
        zipWithHistoricalOffset(input)
                .filter(ho -> Optional.ofNullable(max.get()).map(_m -> _m.getPayload().getClose().compareTo(ho.getPayload().getClose()) < 0).orElse(true))
                .forEach(max::set);
        return Optional.ofNullable(max.get()).flatMap(ho -> Lists.last(input).map(latest -> new RecentHigh(ho, new HistoricalOffset<>(0,latest))));
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
