package com.github.dfauth.ta.functions;

import com.github.dfauth.ta.functional.Tuple2;
import com.github.dfauth.ta.model.PriceAction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.dfauth.ta.functional.Lists.mapList;
import static com.github.dfauth.ta.functional.Lists.splitAt;
import static com.github.dfauth.ta.functions.RSI.calculateRSI;
import static com.github.dfauth.ta.functions.RSI.rsi;
import static com.github.dfauth.ta.util.BigDecimalOps.collect;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RSITest {

    @Test
    public void testIt() {
        assertEquals(
                collect(55.3, 51.1, 38.4, 49.3, 40.4, 42.8, 57.2, 60.3, 41.1, 44.7, 35.6, 34.3, 29.4, 26.1, 31.3, 32.7, 37.5, 39.7, 37.0, 34.3, 32.4, 22.8, 34.7, 45.6, 42.5, 49.8, 48.4, 51.9, 45.4, 51.1, 48.0, 39.1, 47.0, 57.7, 61.0, 71.1, 71.1, 72.6, 80.3, 75.0, 78.8, 78.9, 69.8, 56.3, 54.8, 59.8, 61.3, 66.0, 66.7, 53.8, 54.8, 51.6, 45.7, 40.8, 40.8, 37.6, 42.1, 49.2, 51.9, 42.5, 40.2, 27.7, 20.1, 38.3, 35.9, 44.2, 60.2, 64.1, 71.0, 72.3, 74.3, 69.4, 61.9, 67.6, 65.1, 65.5, 71.3, 71.3, 44.6, 39.8, 45.5, 41.3, 34.4, 38.0, 36.8, 35.7, 37.6, 37.1, 39.7, 39.1, 39.5, 34.7, 54.6, 58.2, 43.1, 48.6, 46.7, 36.9, 38.4, 43.1, 40.1, 42.0, 43.5, 32.7, 18.4, 22.5, 23.4, 22.8, 22.5, 20.8, 21.5, 24.5, 17.6, 17.9, 25.5, 20.1, 13.5, 20.8, 28.8, 21.3, 31.2, 32.5, 41.0, 39.7, 43.7, 39.6, 42.4, 50.6, 49.0, 56.2, 57.1, 59.8, 55.6, 61.9, 54.1, 54.1, 46.8, 45.8, 74.8, 77.4, 77.0, 70.8, 69.2, 70.4, 71.4, 65.7, 65.0, 68.1, 71.9, 68.7, 70.7, 74.3, 43.3, 48.4, 50.5, 59.6, 61.6, 77.4, 79.4, 78.4, 78.9, 80.1, 80.3, 86.5, 86.3, 88.0, 86.4, 81.0, 80.0, 81.7, 80.8, 72.0, 67.9, 82.2, 84.9, 77.3, 52.6, 46.8, 44.3, 39.4, 44.2, 49.7, 53.1, 44.5, 46.9, 45.7, 39.5, 27.1, 23.0, 63.6, 77.5, 80.9, 81.2, 81.2, 80.9, 81.3, 81.6, 80.5, 73.9, 72.6, 75.0, 83.4, 84.2, 65.8, 63.1, 61.6, 64.0, 62.4, 60.5, 59.8, 58.2, 56.5, 64.3, 71.6, 75.6, 45.6, 54.0, 64.1, 49.8, 66.9, 64.9, 69.9, 65.6, 61.2, 61.2, 63.2, 64.3, 64.0, 57.7, 64.7, 62.2, 57.6, 65.7, 30.6, 38.3, 29.5, 35.2, 38.6, 36.2, 35.7, 29.1, 26.5, 30.4, 39.6, 35.4, 39.1, 43.6, 75.7),
                mapList(TestData.MP1, PriceAction::getClose).stream()
                        .map(rsi())
                        .flatMap(Optional::stream)
                        .collect(Collectors.toList())
        );
    }

    @Test
    public void testWGX() {
        int period = 14+1;
        Tuple2<List<BigDecimal>, List<BigDecimal>> subList = splitAt(mapList(TestData.WGX, PriceAction::getClose), TestData.WGX.size()-period);
        assertEquals(
                37.03d,
                calculateRSI(subList._2().stream(),period).get().doubleValue(),
                0.01d
        );
    }

}
