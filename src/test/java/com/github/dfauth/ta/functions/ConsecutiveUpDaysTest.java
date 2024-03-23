package com.github.dfauth.ta.functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.dfauth.ta.model.Price;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.dfauth.ta.functions.TestData.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class ConsecutiveUpDaysTest {

    @Test
    public void testSanity() {
        {
            assertEquals(2, consecutiveUpDays(CGC));
        }
    }

    @Test
    public void testIt() throws JsonProcessingException {
        assertEquals(1, consecutiveUpDays(WGX));
        assertEquals(0, consecutiveUpDays(LBL));
        assertEquals(2, consecutiveUpDays(CGC));
        assertEquals(2, consecutiveUpDays(EMR));
        assertEquals(2, consecutiveUpDays(MP1));
        assertEquals(0, consecutiveUpDays(AX1));
        assertEquals(0, consecutiveUpDays(PPL));
    }

    private int consecutiveUpDays(List<Price> prices) {
        return prices.stream().collect(ConsecutiveUpDays.collector());
    }
}
