package com.github.dfauth.ta.functions;

import com.github.dfauth.ta.functional.PriceActionFunction;
import com.github.dfauth.ta.functional.PriceActionFunctions;
import com.github.dfauth.ta.model.Price;
import com.github.dfauth.ta.model.PriceAction;
import com.github.dfauth.ta.util.ArrayRingBuffer;
import com.github.dfauth.ta.util.RingBuffer;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Optional;

import static java.time.Instant.now;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PriceActionFunction2Test {

    private static final Logger logger = LoggerFactory.getLogger(PriceActionFunction2Test.class);

    @Test
    public void testIt() {
        PriceActionFunction<PriceAction, PriceAction, PriceAction> f = PriceActionFunctions.match(new HashMap<>(),"sma(23)").orElseThrow();
        assertNotNull(f);
        RingBuffer<PriceAction> ringBuffer = new ArrayRingBuffer<>(new PriceAction[23]);
        for(int i=0; i<23;i++) {
            ringBuffer.write(newPrice(i));
        }
        Optional<PriceAction> sma = ringBuffer.calculate(f);
        assertNotNull(sma.get());
        assertEquals(11, sma.get().getClose().intValue());
        assertEquals(11, sma.get().getVolume());
    }

    private PriceAction newPrice(int i) {
        return new Price("CODE", Timestamp.from(now()), BigDecimal.valueOf(i), BigDecimal.valueOf(i), BigDecimal.valueOf(i), BigDecimal.valueOf(i), i);
    }

}
