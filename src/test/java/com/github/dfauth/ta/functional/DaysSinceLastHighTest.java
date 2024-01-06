package com.github.dfauth.ta.functional;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dfauth.ta.functions.TestData;
import com.github.dfauth.ta.model.Price;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.github.dfauth.ta.functional.Lists.mapList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class DaysSinceLastHighTest {

    private static ZonedDateTime now = ZonedDateTime.of(2023,9,1,0,0,0,0, ZoneId.of("UTC"));

    @Test
    public void testIt() {
        assertEquals(Optional.of(-26), DaysSince.lastHigh(mapList(TestData.MP1,Price::getClose)));
    }

    @Test
    public void testRecentHigh() throws JsonProcessingException {
        DaysSince.RecentHigh recentHigh = DaysSince.recentHigh(TestData.MP1).get();
        Map<String,Object> ref = Map.of("date","2023-08-24","daysSince",-26,"pctBelow",-0.053,"price",12.430);
        assertTrue(jsonEquivalence(ref, recentHigh));
    }

    private boolean jsonEquivalence(Map<String, Object> ref, Object jsonObject) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return ref.equals(mapper.readValue(mapper.writeValueAsString(jsonObject), HashMap.class));
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

}
