package com.github.dfauth.model;

import com.github.dfauth.ta.functions.RSI;
import com.github.dfauth.ta.model.Dated;
import com.github.dfauth.ta.util.BigDecimalOps;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.github.dfauth.ta.model.Dated.dated;
import static com.github.dfauth.ta.model.MarketEnum.ASX;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
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
//        assertTrue(dated1.map(BigDecimalOps::add).apply(dated2).isEmpty());
//        assertTrue(dated1.map(BigDecimalOps::add).apply(dated3).isPresent());
//        assertEquals(3, dated1.map(BigDecimalOps::add).apply(dated3).map(Dated::getPayload).map(BigDecimal::intValue).get());
    }

    @Test
    public void testDatedGainLoss() {

        Dated<RSI.GainLoss> dgl = dated(RSI.GainLoss.create(ZERO, ONE));
        assertEquals(dgl.getLocalDate(), LocalDate.now());
        assertEquals(1,dgl.getPayload().getGain().intValue());
        assertEquals(0,dgl.getPayload().getLoss().intValue());

        Dated<RSI.GainLoss> prev = dated(now().minusDays(1), RSI.GainLoss.create(ONE, ZERO));
        assertTrue(prev.getLocalDate().isBefore(dgl.getLocalDate()));
        assertEquals(0, prev.getPayload().getGain().intValue());
        assertEquals(1, prev.getPayload().getLoss().intValue());

        {
            Dated<RSI.GainLoss> q = prev.flatMap(_p -> dgl.map(_p::add));
            assertEquals(q.getLocalDate(), dgl.getLocalDate());
            assertEquals(1, q.getPayload().getGain().intValue());
            assertEquals(1, q.getPayload().getLoss().intValue());
        }
        {
            Dated<RSI.GainLoss> q = prev.map(dgl, RSI.GainLoss::addStatic);
            assertEquals(q.getLocalDate(), dgl.getLocalDate());
            assertEquals(1, q.getPayload().getGain().intValue());
            assertEquals(1, q.getPayload().getLoss().intValue());
        }
    }

}
