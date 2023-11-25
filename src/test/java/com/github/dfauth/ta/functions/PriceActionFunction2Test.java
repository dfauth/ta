package com.github.dfauth.ta.functions;

import com.github.dfauth.ta.functional.IdentityPriceActionFunctions;
import com.github.dfauth.ta.model.Price;
import com.github.dfauth.ta.model.PriceAction;
import com.github.dfauth.ta.util.CalculatingRingBuffer;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Optional;

import static java.time.Instant.now;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Slf4j
public class PriceActionFunction2Test {

    @Test
    public void testIt() {
        CalculatingRingBuffer<PriceAction, PriceAction, PriceAction> ringBuffer = IdentityPriceActionFunctions.match(new HashMap<>(), "sma(23)").orElseThrow();
        assertNotNull(ringBuffer);
        for(int i=0; i<23;i++) {
            ringBuffer.write(newPrice(i));
        }
        Optional<PriceAction> sma = ringBuffer.calculate();
        assertNotNull(sma.get());
        assertEquals(11, sma.get().getClose().intValue());
        assertEquals(11, sma.get().getVolume());
    }

    private PriceAction newPrice(int i) {
        return new Price("CODE", Timestamp.from(now()), BigDecimal.valueOf(i), BigDecimal.valueOf(i), BigDecimal.valueOf(i), BigDecimal.valueOf(i), i);
    }

}
