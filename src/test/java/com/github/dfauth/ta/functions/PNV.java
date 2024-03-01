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
        return toPrices(code, priceString).stream().map(Price::get_close).collect(Collectors.toList()).toArray(BigDecimal[]::new);
    }

    @Test
    public void testRSI() {
        int period = 14;
        List<Dated<BigDecimal>> window = mapList(TestData.MP1, p -> dated(p.getDate(), p.getClose()));
        Optional<Dated<BigDecimal>> rsi = RSI.calculateRSI(window.stream(), period);
        assertTrue(rsi.isPresent());
        assertEquals(YAHOO_RSI.doubleValue(), rsi.orElseThrow().getPayload().doubleValue(), YAHOO_RSI.doubleValue()*0.01);
    }

    @Test
    public void testLobf() {
        List<BigDecimal> window = mapList(TestData.MP1, p -> p.getClose());
        Optional<LinearRegression.LineOfBestFit> lobf = LinearRegression.lobf(window);
        assertNotNull(lobf);

        Optional<com.github.dfauth.ta.functions.ref.LinearRegression> ref = com.github.dfauth.ta.functions.ref.LinearRegression.calculate(window, BigDecimal::doubleValue);
        assertEquals(BigDecimal.valueOf(ref.get().getSlope()), lobf.get().getSlope());
//        assertEquals(ref.intercept(), lobf.get().getIntercept().doubleValue(), ref.intercept()*0.01);
//        assertEquals(ref.R2(), lobf.get().getR2().doubleValue(), ref.R2()*0.01);
//        assertEquals(ref.slopeStdErr(), lobf.get().getSlopeStdErr().doubleValue(), ref.slopeStdErr()*0.01);
//        assertEquals(ref.interceptStdErr(), lobf.get().getInterceptStdErr().doubleValue(), ref.interceptStdErr()*0.01);
//        assertEquals(ref.predict(i.get()), lobf.get().predict(i.get()).doubleValue(), ref.predict(i.get())*0.01);
    }
}
