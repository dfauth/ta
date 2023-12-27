package com.github.dfauth.model;

import com.github.dfauth.ta.model.Candlestick;
import com.github.dfauth.ta.model.Price;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static com.github.dfauth.ta.functions.TestUtils.bdOf;
import static com.github.dfauth.ta.functions.TestUtils.dateOf;
import static com.github.dfauth.ta.model.CandlestickComparator.REDUCED_VOLUME;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class CandlestickTest {

    public static final Candlestick dayMinus2 = new Price("CODE", dateOf(2022,10,1), bdOf(10.1), bdOf(10.35), bdOf(9.93), bdOf(10.13), 1000);
    public static final Candlestick dayMinus1 = new Price("CODE", dateOf(2022,10,2), bdOf(10.11), bdOf(10.31), bdOf(9.91), bdOf(9.98), 730);

    public static final Candlestick day0 = new Price("CODE", dateOf(2022,10,3), bdOf(10.07), bdOf(10.59), bdOf(10.07), bdOf(10.57), 1230);

    @Test
    public void testIt() {
        assertThrows(IllegalArgumentException.class, () -> dayMinus2.closedHigher(dayMinus1));
        assertTrue(dayMinus1.closedLower(dayMinus2));
        assertFalse(dayMinus1.closedHigher(dayMinus2));
        assertTrue(REDUCED_VOLUME.curry(dayMinus2).apply(dayMinus1));
        assertFalse(REDUCED_VOLUME.curry(dayMinus1).apply(day0));
    }

}
