package com.github.dfauth.ta.functions;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.github.dfauth.ta.functional.HistoricalOffset;
import com.github.dfauth.ta.functional.Reducer;
import com.github.dfauth.ta.model.Price;
import com.github.dfauth.ta.util.BigDecimalOps;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.github.dfauth.ta.functional.HistoricalOffset.zipWithPrevious;
import static com.github.dfauth.ta.util.BigDecimalOps.isGreaterThan;
import static com.github.dfauth.ta.util.BigDecimalOps.isGreaterThanOrEqualTo;

public class ConsecutiveUpDays implements Reducer<HistoricalOffset<Price>, ConsecutiveUpDays.Interim,List<ConsecutiveUpDays.PriceInterval>> {

    private List<PriceInterval> output = new ArrayList<>();

    public static int consecutiveUpDays(List<Price> prices) {
        final AtomicInteger r = new AtomicInteger(0);
        r.set(zipWithPrevious(prices)
                .map(z -> {
                    r.set(Optional.of(r.get())
                            .filter(_r -> isGreaterThan(z.getCurrent().getClose(), z.getPrevious().getClose()))
                            .map(_r -> r.incrementAndGet())
                            .orElse(0));
                    return r.get();
                })
                .reduce((i1,i2)-> i2).orElse(0));
        return r.get();
    }

    @Override
    public Interim initial() {
        return new Interim();
    }

    @Override
    public Function<Interim, List<PriceInterval>> finisher() {
        return i -> {
            i.getPriceInterval().ifPresent(output::add);
            return output;
        };
    }

    @Override
    public BiConsumer<Interim, HistoricalOffset<Price>> accumulator() {
        return (i,p) -> i.nextDay(p).ifPresent(output::add);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode
    @ToString
    public static class Interim {
        private int streak;
        private HistoricalOffset<Price> start;
        private HistoricalOffset<Price> previous;

        public Optional<PriceInterval> nextDay(HistoricalOffset<Price> next) {
            if(start == null) {
                start = next;
                streak = 0;
                return Optional.empty();
            } else {
                if(previous == null) {
                    if(isGreaterThanOrEqualTo(next.getPayload().getClose(),start.getPayload().getClose())){
                        streak = 1;
                        previous = next;
                        return Optional.empty();
                    } else {
                        Optional<PriceInterval> result = Optional.of(new PriceInterval(start, next));
                        streak = 0;
                        start = next;
                        previous = null;
                        return result.filter(pi -> pi.getConsecutiveUpDays()>1);
                    }
                } else if(isGreaterThanOrEqualTo(next.getPayload().getClose(),previous.getPayload().getClose())) {
                    streak++;
                    previous = next;
                    return Optional.empty();
                } else {
                    Optional<PriceInterval> result = Optional.of(new PriceInterval(start, previous));
                    streak = 0;
                    start = next;
                    previous = null;
                    return result.filter(pi -> pi.getConsecutiveUpDays()>1);
                }
            }
        }

        public Optional<PriceInterval> getPriceInterval() {
            if(start != null && previous != null) {
                return Optional.of(new PriceInterval(start, previous));
            } else {
                return Optional.empty();
            }
        }
    }

    @Data
    @AllArgsConstructor
    @EqualsAndHashCode
    @ToString
    public static class PriceInterval {
        @JsonIgnore private final HistoricalOffset<Price> start;
        @JsonIgnore private final HistoricalOffset<Price> end;

        public boolean isCurrent() {
            return end.getOffset() <= 1;
        }

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        @JsonSerialize(using= LocalDateSerializer.class)
        @JsonDeserialize(using= LocalDateDeserializer.class)
        public LocalDate getStart() {
            return start.getPayload().getDate();
        }

        public BigDecimal getStartPrice() {
            return start.getPayload().getClose();
        }

        public BigDecimal getEndPrice() {
            return end.getPayload().getClose();
        }

        public int getConsecutiveUpDays() {
            return start.getOffset()-end.getOffset();
        }

        public BigDecimal getPctGain() {
            return BigDecimalOps.pctChange(end.getPayload().getClose(), start.getPayload().getClose());
        }
    }
}
