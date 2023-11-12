package com.github.dfauth.ta.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.util.List;
import java.util.Optional;

import static com.github.dfauth.ta.functional.HistoricalOffset.zipWithHistoricalOffset;
import static com.github.dfauth.ta.functional.Lists.last;
import static com.github.dfauth.ta.functions.TestData.*;
import static org.junit.Assert.assertEquals;

@Slf4j
public class ConsecutiveUpDaysTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testIt() throws JsonProcessingException {
//        {
//            int consecutiveUpDays = consecutiveUpDays(CGC);
//            log.info(" consecutive up days: {}",mapper.writeValueAsString(consecutiveUpDays));
//        }
        {
            List<ConsecutiveUpDays.PriceInterval> consecutiveUpDays = zipWithHistoricalOffset(CGC).filter(hop -> hop.getPayload().getDate().isAfter(ChronoLocalDate.from(LocalDate.of(2023,9,14)))).collect(new ConsecutiveUpDays());
            log.info(" consecutive up days: {}",mapper.writeValueAsString(consecutiveUpDays));
            assertEquals(9, last(consecutiveUpDays).filter(ConsecutiveUpDays.PriceInterval::isCurrent).map(ConsecutiveUpDays.PriceInterval::getConsecutiveUpDays).orElse(0).intValue());
        }
        {
            Optional<ConsecutiveUpDays.PriceInterval> consecutiveUpDays = last(zipWithHistoricalOffset(EMR).collect(new ConsecutiveUpDays()));
            log.info(" consecutive up days: {}",mapper.writeValueAsString(consecutiveUpDays.get()));
            assertEquals(3, consecutiveUpDays.filter(ConsecutiveUpDays.PriceInterval::isCurrent).map(ConsecutiveUpDays.PriceInterval::getConsecutiveUpDays).orElse(0).intValue());
        }
        {
            Optional<ConsecutiveUpDays.PriceInterval> consecutiveUpDays = last(zipWithHistoricalOffset(MP1).collect(new ConsecutiveUpDays()));
            log.info(" consecutive up days: {}",mapper.writeValueAsString(consecutiveUpDays.get()));
            assertEquals(2, consecutiveUpDays.filter(ConsecutiveUpDays.PriceInterval::isCurrent).map(ConsecutiveUpDays.PriceInterval::getConsecutiveUpDays).orElse(0).intValue());
        }
        {
            Optional<ConsecutiveUpDays.PriceInterval> consecutiveUpDays = last(zipWithHistoricalOffset(AX1).collect(new ConsecutiveUpDays()));
            log.info(" consecutive up days: {}",mapper.writeValueAsString(consecutiveUpDays.get()));
            assertEquals(0, consecutiveUpDays.filter(ConsecutiveUpDays.PriceInterval::isCurrent).map(ConsecutiveUpDays.PriceInterval::getConsecutiveUpDays).orElse(0).intValue());
        }
        {
            Optional<ConsecutiveUpDays.PriceInterval> consecutiveUpDays = last(zipWithHistoricalOffset(PPL).collect(new ConsecutiveUpDays()));
            log.info(" consecutive up days: {}",mapper.writeValueAsString(consecutiveUpDays.get()));
            assertEquals(0, consecutiveUpDays.filter(ConsecutiveUpDays.PriceInterval::isCurrent).map(ConsecutiveUpDays.PriceInterval::getConsecutiveUpDays).orElse(0).intValue());
        }
    }
}
