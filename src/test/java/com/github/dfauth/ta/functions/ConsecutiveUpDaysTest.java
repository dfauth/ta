package com.github.dfauth.ta.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.dfauth.ta.model.Price;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Supplier;

import static com.github.dfauth.ta.functional.Lists.mapList;
import static com.github.dfauth.ta.functions.ConsecutiveUpDays.consecutiveUpDays;
import static com.github.dfauth.ta.functions.TestData.*;
import static com.github.dfauth.ta.util.BigDecimalOps.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class ConsecutiveUpDaysTest {

    @Test
    public void testSanity() {
        {
            Supplier<BigDecimal> generator = generator(100);
            List<BigDecimal> CGC1 = mapList(CGC, p -> p.mapPrices(bd -> generator.get()).getClose());
            assertEquals(251, consecutiveUpDays(CGC1).get());
        }
        {
            Supplier<BigDecimal> generator = generator(100);
            List<BigDecimal> CGC1 = mapList(CGC, p -> p.mapPrices(bd -> subtract(HUNDRED, generator.get())).getClose());
            assertEquals(0, consecutiveUpDays(CGC1).get());
        }
        {
            Supplier<BigDecimal> generator = generator(100);
            List<BigDecimal> CGC1 = mapList(CGC, p -> p.mapPrices(bd -> generator.get()).getClose());
            CGC1.set(251-3, ZERO3);
            assertEquals(3, consecutiveUpDays(CGC1).get());
        }
        {
            Supplier<BigDecimal> generator = generator(100);
            List<BigDecimal> CGC1 = mapList(CGC, p -> p.mapPrices(bd -> generator.get()).getClose());
            CGC1.set(251-69, ZERO3);
            assertEquals(69, consecutiveUpDays(CGC1).get());
        }
    }

    @Test
    public void testIt() throws JsonProcessingException {
        assertEquals(241, consecutiveUpDays(mapList(CGC, Price::getClose)).get()); // 9
        assertEquals(245, consecutiveUpDays(mapList(EMR, Price::getClose)).get()); // 3
        assertEquals(128, consecutiveUpDays(mapList(MP1, Price::getClose)).get()); // 2
        assertEquals(244, consecutiveUpDays(mapList(AX1, Price::getClose)).get()); // 0
        assertEquals(102, consecutiveUpDays(mapList(PPL, Price::getClose)).get()); // 102
    }
}
