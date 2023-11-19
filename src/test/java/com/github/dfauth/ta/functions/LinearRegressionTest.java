package com.github.dfauth.ta.functions;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertEquals;

public class LinearRegressionTest {

    private double tolerance = 0.03d;

    @Test
    public void testIt() {
        LinearRegression.LineOfBestFit result = LinearRegression.lobf(List.of(0, 1, 2, 3, 4, 5).stream().map(BigDecimal::new).collect(Collectors.toList()));
        assertEqualsTolerance(1.0d, result.getSlope(), tolerance);
        assertEqualsTolerance(0.0d, result.getIntercept(), tolerance);
        assertEqualsTolerance(1.0d, result.getR2(), tolerance);
        assertEqualsTolerance(0.0d, result.getInterceptStdErr().doubleValue(), tolerance);
        assertEqualsTolerance(0.0d, result.getSlopeStdErr().doubleValue(), tolerance);
    }

    @Test
    public void testPNV() {
        LinearRegression.LineOfBestFit result = LinearRegression.lobf(Arrays.asList(PNV.PRICES));
        assertEqualsTolerance(0.002668782802162329d, result.getSlope(), tolerance);
        assertEqualsTolerance(1.4714169646778341d, result.getIntercept(), tolerance);
        assertEqualsTolerance(0.24353416623915145d, result.getR2(), tolerance);
        assertEqualsTolerance(0.043152222027654306d, result.getInterceptStdErr().doubleValue(), tolerance);
        assertEqualsTolerance(0.000297d, result.getSlopeStdErr().doubleValue(), tolerance);
    }

    private void assertEqualsTolerance(double ref, double result, double tolerance) {
        assertEquals(ref, result, tolerance*ref);
    }
}
