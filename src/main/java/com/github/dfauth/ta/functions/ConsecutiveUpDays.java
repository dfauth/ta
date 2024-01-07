package com.github.dfauth.ta.functions;

import com.github.dfauth.ta.functional.HistoricalOffset;
import com.github.dfauth.ta.util.BigDecimalOps;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import static com.github.dfauth.ta.functional.Collectors.comparing;
import static com.github.dfauth.ta.functional.HistoricalOffset.zipWithHistoricalOffset;
import static com.github.dfauth.ta.functional.Lists.last;

public class ConsecutiveUpDays {

    public static BiPredicate<BigDecimal,BigDecimal> isUpDay = BigDecimalOps::isLessThan;

    public static BiFunction<HistoricalOffset<BigDecimal>,HistoricalOffset<BigDecimal>,Integer> consecutiveUpDays = (ho1,ho2) ->
            isUpDay.test(ho1.getPayload(),ho2.getPayload()) ? ho2.getOffset() - ho1.getOffset() : 0;

    public static Optional<Integer> consecutiveUpDays(List<BigDecimal> prices) {
        Stream<HistoricalOffset<BigDecimal>> historicalOffsetStream = zipWithHistoricalOffset(prices);
        return last(historicalOffsetStream.map(Interval::new).collect(comparing((i1,i2) -> {
            return isUpDay.test(i1.getStart().getPayload(),i2.getStart().getPayload()) ?
                    new Interval<>(i1.getStart(),i2.getEnd()) :
                    i2;
        }))).map(Interval::getOffset);
    }

    @Data
    @AllArgsConstructor
    public static class Interval<T> {
        private final HistoricalOffset<T> start;
        private final HistoricalOffset<T> end;

        public Interval(HistoricalOffset<T> ho) {
            this(ho,ho);
        }

        public Integer getOffset() {
            return start.duration(end);
        }
    }
}
