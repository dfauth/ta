package com.github.dfauth.ta.functions;

import com.github.dfauth.ta.functional.ForceIndex;
import com.github.dfauth.ta.model.Price;
import com.github.dfauth.ta.model.PriceAction;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static com.github.dfauth.ta.functional.Collectors.EMA;
import static com.github.dfauth.ta.functional.Collectors.SMA;
import static com.github.dfauth.ta.functional.Lists.mapList;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class ForceIndexTest {

    @Test
    public void testForceIndex() {
        List<PriceAction> result = mapList(TestData.EMR, PriceAction.class::cast);
        assertEquals(41649.57153846154, ForceIndex.calculateForceIndex(result,13,2).getLongPeriod().doubleValue(), 0.0001d);
        assertEquals(2.65, ForceIndex.calculateForceIndex(result,13,2).idx().doubleValue(), 0.0001d);
    }

    @Test
    public void testEMAClose() {
        List<BigDecimal> result = mapList(TestData.EMR, Price::getClose);
        assertEquals(1.879021207373472, EMA.apply(result).doubleValue(), 0.0001d);
    }

    @Test
    public void testSMAClose() {
        List<BigDecimal> result = mapList(TestData.EMR, Price::getClose);
        assertEquals(1.6592460317460318, SMA.apply(result).doubleValue(), 0.0001d);
    }
}
