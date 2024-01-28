package com.github.dfauth.model;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.github.dfauth.ta.model.MarketEnum.ASX;
import static com.github.dfauth.ta.util.DateOps.formatDate;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class MarketTest {

    @Test
    public void testDates() {
        assertTrue(ASX.isOpen(LocalDateTime.of(2024,1,24,12,0).atZone(ASX.getZone())));
        assertFalse(ASX.isOpen(LocalDateTime.of(2024,1,24,18,0).atZone(ASX.getZone())));
        assertTrue(ASX.isOpen(LocalDateTime.of(2024,1,24,10,0).atZone(ASX.getZone())));
        assertFalse(ASX.isOpen(LocalDateTime.of(2024,1,24,16,0).atZone(ASX.getZone())));
        assertTrue(ASX.isOpen(LocalDate.of(2024,1,24)));
        assertFalse(ASX.isOpen(LocalDate.of(2024,1,20)));
        assertEquals("20240124", formatDate(ASX.getMarketDate(LocalDateTime.of(2024,1,24,12,0).atZone(ASX.getZone()))));
        assertEquals("20240124", formatDate(ASX.getMarketDate(LocalDateTime.of(2024,1,24,18,0).atZone(ASX.getZone()))));
        assertEquals("20240125", formatDate(ASX.getMarketDate(LocalDate.of(2024,1,25))));
        assertTrue(ASX.isOpen(LocalDate.of(2024,1,26)));
        assertFalse(ASX.withHolidays(List.of(LocalDate.of(2024,1,26))).isOpen(LocalDate.of(2024,1,26)));
        assertTrue(ASX.withHolidays(List.of(LocalDate.of(2024,1,26))).isOpen(LocalDate.of(2024,1,25)));
    }

}
