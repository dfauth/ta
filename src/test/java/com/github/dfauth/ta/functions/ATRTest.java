package com.github.dfauth.ta.functions;

import com.github.dfauth.ta.functional.ATR;
import com.github.dfauth.ta.model.Price;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.math.BigDecimal.ZERO;
import static java.time.LocalTime.now;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class ATRTest {

    /**
     0, 0,21.51
     21.95,20.22,22.25
     21.10,21.61,21.50
     20.34,20.83,23.25
     22.13,22.65,23.03
     21.87,22.41,23.34
     22.18,22.67,23.66
     22.57,23.05,23.97
     22.80,23.31,24.29
     23.15,23.68,24.60
     23.45,23.97,24.92
     23.76,24.31,25.23
     24.09,24.60,25.55
     24.39,24.89,25.86
     24.69,25.20,0

     */
    @Test
    public void testIt() {
        List<Price> tmp = new ArrayList<>();
        tmp.add(price(0, 0,21.51));
        tmp.add(price(21.95,20.22,22.25));
        tmp.add(price(21.10,21.61,21.50));
        tmp.add(price( 20.34,20.83,23.25));
        tmp.add(price(22.13,22.65,23.03));
        tmp.add(price(21.87,22.41,23.34));
        tmp.add(price(22.18,22.67,23.66));
        tmp.add(price(22.57,23.05,23.97));
        tmp.add(price(22.80,23.31,24.29));
        tmp.add(price(23.15,23.68,24.60));
        tmp.add(price(23.45,23.97,24.92));
        tmp.add(price(23.76,24.31,25.23));
        tmp.add(price(24.09,24.60,25.55));
        tmp.add(price(24.39,24.89,25.86));
        tmp.add(price(24.69,25.20,24.87));
        tmp.add(price(25.55,24.37,0));

        Optional<ATR.AverageTrueRange> atr = ATR.trueRange(tmp, 14);
        log.info("atr: {}",atr.get().getAtr());
        assertEquals(BigDecimal.valueOf(1.19),atr.get().getAtr());
    }

    private Price price(double hi, double lo, double close) {
        return new Price("BLAH", new Timestamp(System.currentTimeMillis()), ZERO, BigDecimal.valueOf(hi), BigDecimal.valueOf(lo), BigDecimal.valueOf(close), 0);
    }
}
