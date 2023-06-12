package com.github.dfauth.ta.functions.ref;

import com.github.dfauth.ta.functions.PNV;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.github.dfauth.ta.functions.ref.LinearRegression.calculate;
import static org.junit.Assert.assertEquals;

public class LinearRegressionTest {

    @Test
    public void testIt() {
        Optional<LinearRegression> result = calculate(List.of(0,1, 2, 3, 4, 5), Integer::doubleValue);
        assertEquals(1.0d, result.map(LinearRegression::getSlope).orElseThrow(), 0.01d);
        assertEquals(0.0d, result.map(LinearRegression::getIntercept).orElseThrow(), 0.01d);
        assertEquals(1.0d, result.map(LinearRegression::getR2).orElseThrow(), 0.01d);
        assertEquals(0.0d, result.map(LinearRegression::interceptStdErr).orElseThrow(), 0.01d);
        assertEquals(0.0d, result.map(LinearRegression::slopeStdErr).orElseThrow(), 0.01d);
    }

    @Test
    public void testPNV() {
        Optional<LinearRegression> result = calculate(Arrays.asList(PNV.PRICES), BigDecimal::doubleValue);
        assertEquals(0.002668782802162329d, result.map(LinearRegression::getSlope).orElseThrow(), 0.01d);
        assertEquals(1.4714169646778341d, result.map(LinearRegression::getIntercept).orElseThrow(), 0.01d);
        assertEquals(0.24353416623915145d, result.map(LinearRegression::getR2).orElseThrow(), 0.01d);
        assertEquals(0.043152222027654306d, result.map(LinearRegression::interceptStdErr).orElseThrow(), 0.01d);
        assertEquals(0.000297d, result.map(LinearRegression::slopeStdErr).orElseThrow(), 0.00001d);
    }
}
