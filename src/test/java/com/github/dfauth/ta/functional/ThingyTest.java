package com.github.dfauth.ta.functional;


import com.github.dfauth.ta.functions.TestData;
import com.github.dfauth.ta.model.PriceAction;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static com.github.dfauth.ta.functional.Lists.mapList;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class ThingyTest {

    @Test
    public void testIt() {
        assertEquals(Thingy.ThingyState.BULLISH, Thingy.calculateThingy(mapList(TestData.MP1, PriceAction.class::cast)).getState());
        assertEquals(Thingy.ThingyState.BULLISH, Thingy.calculateThingy(mapList(TestData.EMR, PriceAction.class::cast)).getState());
        assertEquals(Thingy.ThingyState.BEARISH, Thingy.calculateThingy(mapList(TestData.LBL, PriceAction.class::cast)).getState());
        assertEquals(Thingy.ThingyState.BEARISH_RECOVERY, Thingy.calculateThingy(mapList(TestData.PPL, PriceAction.class::cast)).getState());
        assertEquals(Thingy.ThingyState.BULLISH, Thingy.calculateThingy(mapList(TestData.WGX, PriceAction.class::cast)).getState());
        assertEquals(Thingy.ThingyState.BULLISH_FADEOUT, Thingy.calculateThingy(mapList(TestData.CGC, PriceAction.class::cast)).getState());
        assertEquals(Thingy.ThingyState.BEARISH_RECOVERY, Thingy.calculateThingy(mapList(TestData.AX1, PriceAction.class::cast)).getState());
    }
}
