package com.github.dfauth.ta.functions;

import com.github.dfauth.ta.functional.MACD;
import com.github.dfauth.ta.functional.Tuple2;
import com.github.dfauth.ta.model.PriceAction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.dfauth.ta.functional.Lists.mapList;
import static com.github.dfauth.ta.functional.Lists.splitAt;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MACDTest {

    @Test
    public void testWGX() {
        int period = 26;
        Tuple2<List<PriceAction>, List<PriceAction>> subList = splitAt(mapList(TestData.WGX, PriceAction.class::cast), TestData.WGX.size() - period);
        MACD macd = MACD.calculateMACD(subList._2().stream()).get();
        assertEquals(
                -0.032d,
                macd.getMacd().doubleValue(),
                0.001d
        );
        assertEquals(
                0.003d,
                macd.getSignal().doubleValue(),
                0.001d
        );
    }

}
