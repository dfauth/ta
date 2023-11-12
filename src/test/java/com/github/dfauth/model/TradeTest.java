package com.github.dfauth.model;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
public class TradeTest {


    private static final String TEST_DATE = "2023-05-16T00:00:00+08:00";

    @Test
    public void testIt() {
        Timestamp ts = new Timestamp(LocalDate.parse(TEST_DATE, DateTimeFormatter.ISO_DATE_TIME).atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli());
        log.info("ts is {}",ts);
    }
}
