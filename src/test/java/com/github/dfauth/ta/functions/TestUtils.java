package com.github.dfauth.ta.functions;

import com.github.dfauth.ta.util.BigDecimalOps;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;

@Slf4j
public class TestUtils {

    public static final Timestamp dateOf(int yr, int mn, int dt) {
        return dateOf(yr,mn,dt,ZoneId.of("Australia/Sydney"));
    }

    public static final Timestamp dateOf(int yr, int mn, int dt, ZoneId zid) {
        return new Timestamp(LocalDate.of(yr, mn, dt).atTime(16,0).atZone(zid).toInstant().toEpochMilli());
    }

    public static final BigDecimal bdOf(double d) {
        return BigDecimal.valueOf(d);
    }

    public static final BigDecimal bdOf(int i) {
        return scale(BigDecimal.valueOf(i));
    }

    public static final BigDecimal scale(BigDecimal db) {
        return BigDecimalOps.scale(db,3);
    }

}
