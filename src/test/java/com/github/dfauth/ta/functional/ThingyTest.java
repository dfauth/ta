package com.github.dfauth.ta.functional;


import com.github.dfauth.ta.model.Price;
import com.github.dfauth.ta.model.PriceAction;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.dfauth.ta.functional.Lists.mapList;
import static com.github.dfauth.ta.functional.Thingy.calculateThingy;
import static com.github.dfauth.ta.functions.TestData.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class ThingyTest {

    @Test
    public void testIt() {
        {
            Thingy thingy = thingy(MP1);
            assertEquals(Thingy.ThingyState.BULLISH, thingy.getState());
            assertEquals(52, thingy.getDaysInThisState());
            assertTrue(thingy.getPreviousState().isEmpty());
            assertEquals(0.041, thingy.getDistanceFromMA().doubleValue());
            assertFalse(thingy.isTrendAccelerating());
        }

        {
            Thingy thingy = thingy(EMR);
            assertEquals(Thingy.ThingyState.BULLISH, thingy.getState());
            assertEquals(52, thingy.getDaysInThisState());
            assertTrue(thingy.getPreviousState().isEmpty());
            assertEquals(0.05, thingy.getDistanceFromMA().doubleValue());
            assertTrue(thingy.isTrendAccelerating());
        }

        {
            Thingy thingy = thingy(LBL);
            assertEquals(Thingy.ThingyState.BEARISH, thingy.getState());
            assertEquals(10, thingy.getDaysInThisState());
            assertEquals(Thingy.ThingyState.BULLISH_FADEOUT, thingy.getPreviousState().get());
            assertEquals(0.003, thingy.getDistanceFromMA().doubleValue());
            assertTrue(thingy.isTrendAccelerating());
        }

        {
            Thingy thingy = thingy(PPL);
            assertEquals(Thingy.ThingyState.BEARISH_RECOVERY, thingy.getState());
            assertEquals(52, thingy.getDaysInThisState());
            assertTrue(thingy.getPreviousState().isEmpty());
            assertEquals(0.0, thingy.getDistanceFromMA().doubleValue());
            assertFalse(thingy.isTrendAccelerating());
        }

        {
            Thingy thingy = thingy(WGX);
            assertEquals(Thingy.ThingyState.BULLISH, thingy.getState());
            assertEquals(52, thingy.getDaysInThisState());
            assertTrue(thingy.getPreviousState().isEmpty());
            assertEquals(-0.077, thingy.getDistanceFromMA().doubleValue());
            assertFalse(thingy.isTrendAccelerating());
        }

        {
            Thingy thingy = thingy(CGC);
            assertEquals(Thingy.ThingyState.BULLISH_FADEOUT, thingy.getState());
            assertEquals(20, thingy.getDaysInThisState());
            assertEquals(Thingy.ThingyState.BULLISH, thingy.getPreviousState().get());
            assertEquals(0.053, thingy.getDistanceFromMA().doubleValue());
            assertTrue(thingy.isTrendAccelerating());
        }

        {
            Thingy thingy = thingy(AX1);
            assertEquals(Thingy.ThingyState.BEARISH_RECOVERY, thingy.getState());
            assertEquals(42, thingy.getDaysInThisState());
            assertEquals(Thingy.ThingyState.BEARISH, thingy.getPreviousState().get());
            assertEquals(0.003, thingy.getDistanceFromMA().doubleValue());
            assertFalse(thingy.isTrendAccelerating());
        }
    }

    private Thingy thingy(List<Price> priceAction) {
        Price last = priceAction.get(priceAction.size() - 1);
        log.info(priceAction.size()+" prices for "+last.getCode()+" ending "+last.getDate());
        return calculateThingy(mapList(priceAction, PriceAction.class::cast)).get();
    }
}
