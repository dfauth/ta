package com.github.dfauth.ta.functions;

import com.github.dfauth.ta.util.BigDecimalOps;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Iterator;

import static com.github.dfauth.ta.util.DateOps.WEEKDAYS;

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

    public static Iterator<LocalDate> iterateWeekDaysStarting(LocalDate start) {
        final LocalDate[] date = {start};
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public LocalDate next() {
                LocalDate tmp = date[0];
                do {
                    date[0] = date[0].plusDays(1);
                } while(!WEEKDAYS.contains(date[0].getDayOfWeek()));
                return tmp;
            }
        };
    }
}
