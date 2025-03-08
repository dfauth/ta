package com.github.dfauth.model;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.github.dfauth.ta.model.Side.fromString;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class SidedTest {

    @Test
    public void testIt() {
        assertEquals(3.0, fromString("B").sided(2.0).add(fromString("Sell").sided(5.0)).getValue());
        assertEquals(7.0, fromString("B").sided(2.0).subtract(fromString("S").sided(5.0)).getValue());
        assertEquals(10.0, fromString("B").sided(2.0).multiply(fromString("Buy").sided(5.0)).getValue());
        assertEquals(BigDecimal.valueOf(10.0).setScale(2), fromString("B").sided(2.0).multiply(fromString("Buy").sided(5)).getValue());
        assertEquals(0.4, fromString("Buy").sided(2.0).divide(fromString("S").sided(5.0)).getValue());
        assertEquals(BigDecimal.valueOf(0.4), fromString("Buy").sided(2).divide(fromString("S").sided(5.0)).getValue());
        assertEquals(BigDecimal.valueOf(0.4), fromString("Buy").sided(2).divide(fromString("S").sided(5.0)).getValue());
    }

}
