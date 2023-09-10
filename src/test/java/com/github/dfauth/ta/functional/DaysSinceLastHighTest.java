package com.github.dfauth.ta.functional;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeWithZoneIdSerializer;
import com.github.dfauth.ta.model.Price;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.github.dfauth.ta.functional.Tuple2.tuple2;
import static org.junit.Assert.assertEquals;

@Slf4j
public class DaysSinceLastHighTest {

    private static final int[] TEST_DATA = new int[]{
            1,2,3,1,2,3,1,2,3,4,3,2,1,2,4,9,1
    };
    private static ZonedDateTime now = ZonedDateTime.of(2023,9,1,0,0,0,0, ZoneId.of("UTC"));

    @Test
    public void testIt() {
        assertEquals(Optional.of(1), DaysSince.lastHigh(createTestData(TEST_DATA).stream().map(Price::get_close).collect(Collectors.toList())));
    }

    @Test
    public void testRecentHigh() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        DaysSince.RecentHigh recentHigh = DaysSince.recentHigh(createTestData(TEST_DATA).stream().collect(Collectors.toList())).get();
        assertEquals(
                "{\"high\":[1,{\"open\":11.9,\"volume\":1000,\"high\":11.9,\"close\":11.9,\"code\":\"CODE\",\"low\":11.9,\"date\":\"2023-09-17\",\"rising\":false,\"falling\":false,\"range\":0.0}],\"last\":[0,{\"open\":11.1,\"volume\":1000,\"high\":11.1,\"close\":11.1,\"code\":\"CODE\",\"low\":11.1,\"date\":\"2023-09-18\",\"rising\":false,\"falling\":false,\"range\":0.0}],\"date\":\"2023-09-17\",\"price\":11.9,\"daysSince\":-1,\"pctBelow\":-0.1}",
                mapper.writeValueAsString(recentHigh)
        );
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
