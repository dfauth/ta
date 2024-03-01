package com.github.dfauth.model;

import com.github.dfauth.ta.model.Dated;
import com.github.dfauth.ta.util.BigDecimalOps;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.github.dfauth.ta.model.Dated.dated;
import static com.github.dfauth.ta.model.MarketEnum.ASX;
import static java.time.LocalDate.now;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class DatedTest {

    @Test
    public void testIt() {
        LocalDate now = now();
        LocalDate _then = now.minusDays(1);
        Dated<BigDecimal> dated1 = dated(ASX.getMarketDate(), BigDecimalOps.valueOf(1));
        Dated<BigDecimal> dated2 = dated(ASX.getMarketDate(_then), BigDecimalOps.valueOf(2));
        Dated<BigDecimal> dated3 = dated(ASX.getMarketDate(now), BigDecimalOps.valueOf(2));
        assertTrue(dated1.map(BigDecimalOps::add).apply(dated2).isEmpty());
        assertTrue(dated1.map(BigDecimalOps::add).apply(dated3).isPresent());
        assertEquals(3, dated1.map(BigDecimalOps::add).apply(dated3).map(Dated::getPayload).map(BigDecimal::intValue).get());
    }

}
