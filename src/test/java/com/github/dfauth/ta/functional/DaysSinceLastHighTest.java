package com.github.dfauth.ta.functional;


import com.github.dfauth.ta.model.Price;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

@Slf4j
public class DaysSinceLastHighTest {

    private static final int[] TEST_DATA = new int[]{
            1,2,3,1,2,3,1,2,3,4,3,2,1,2,4,9,1
    };
    private static ZonedDateTime now = null;

    @Test
    public void testIt() {
        assertEquals(Optional.of(1), DaysSince.lastHigh(createTestData(TEST_DATA).stream().map(Price::get_close).collect(Collectors.toList())));
    }

    private static List<Price> createTestData(int[] input) {
        return IntStream.of(input).mapToObj(i -> price(11,i)).collect(Collectors.toList());
    }

    private static Price price(int i, int i1) {
        BigDecimal d = BigDecimal.valueOf(i + (double)i1/10);
        return new Price("CODE", nextDate(), d,d,d,d,1000);
    }

    private static Timestamp nextDate() {
        now = Optional.ofNullable(now).map(_now -> now.plusDays(1)).orElseGet(ZonedDateTime::now);
        return new Timestamp(now.toEpochSecond()*1000);
    }
}
