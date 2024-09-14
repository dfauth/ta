package com.github.dfauth.ta.functions;

import com.github.dfauth.ta.model.Dated;
import com.github.dfauth.ta.model.Price;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.dfauth.ta.functional.Lists.mapList;
import static com.github.dfauth.ta.model.Dated.dated;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class PNV {

    public static final BigDecimal YAHOO_RSI = new BigDecimal("34.3385");

    public static List<Price> toPrices(String code, String priceString) {
        try {
            InputStream bais = new ByteArrayInputStream(priceString.getBytes());
            BufferedReader br = new BufferedReader(new InputStreamReader(bais));
            String line;
            List<Price> tmp = new ArrayList<>();
            while((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                tmp.add(Price.parseStrings(code, new String[]{fields[0],fields[1],fields[2],fields[3],fields[4],fields[6]}));
            }
//            Collections.reverse(tmp);
            return tmp;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public static BigDecimal[] toClosingPrices(String code, String priceString) {
        return toPrices(code, priceString).stream().map(Price::getClose).collect(Collectors.toList()).toArray(BigDecimal[]::new);
    }

    @Test
    public void testRSI() {
        int period = 14;
        List<Dated<BigDecimal>> window = mapList(TestData.MP1, p -> dated(p.getDate(), p.getClose()));
        Optional<Dated<BigDecimal>> rsi = RSI.calculateRSI(window, period);
        assertTrue(rsi.isPresent());
        assertEquals(YAHOO_RSI.doubleValue(), rsi.orElseThrow().getPayload().doubleValue(), YAHOO_RSI.doubleValue()*0.01);
    }

    @Test
    public void testLobf() {
        testLobf(TestData.MP1);
        testLobf(TestData.EMR);
        testLobf(TestData.CGC);
        testLobf(TestData.WGX);
        testLobf(TestData.AX1);
    }

    public void testLobf(List<Price> prices) {
        double tolerance = 0.01d;
        List<BigDecimal> window = mapList(prices, p -> p.getClose());
        Optional<LinearRegression.LineOfBestFit> lobf = LinearRegression.lobf(window);
        assertNotNull(lobf);

        Optional<com.github.dfauth.ta.functions.ref.LinearRegression> ref = com.github.dfauth.ta.functions.ref.LinearRegression.calculate(window, BigDecimal::doubleValue);
        assertEquals(ref.get().getSlope(), lobf.get().getSlope(), lobf.get().getSlope()*tolerance);
        assertEquals(ref.get().getIntercept(), lobf.get().getIntercept(), lobf.get().getIntercept()*tolerance);
        assertEquals(ref.get().getR2(), lobf.get().getR2(), lobf.get().getR2()*tolerance);
        assertEquals(ref.get().slopeStdErr(), lobf.get().getSlopeStdErr().doubleValue(), lobf.get().getSlopeStdErr().doubleValue()*tolerance);
        assertEquals(ref.get().interceptStdErr(), lobf.get().getInterceptStdErr().doubleValue(), lobf.get().getInterceptStdErr().doubleValue()*tolerance);
        assertEquals(ref.get().predict(1), lobf.get().predict(1).doubleValue(), lobf.get().predict(1).doubleValue()*tolerance);
    }
}
