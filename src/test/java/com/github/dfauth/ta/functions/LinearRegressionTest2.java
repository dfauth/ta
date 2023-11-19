package com.github.dfauth.ta.functions;

import com.github.dfauth.ta.model.Price;
import org.junit.Test;

import java.util.Optional;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertEquals;

public class LinearRegressionTest2 {

    private double tolerance = 0.000001d;

    @Test
    public void testIt() {

        Optional<com.github.dfauth.ta.functions.ref.LinearRegression> ref = com.github.dfauth.ta.functions.ref.LinearRegression.calculate(TestData.EMR, p -> p.getClose().doubleValue());

        LinearRegression.LineOfBestFit result = LinearRegression.lobf(TestData.EMR.stream().map(Price::getClose).collect(Collectors.toList()));

        assertEqualsTolerance(
                ref.map(com.github.dfauth.ta.functions.ref.LinearRegression::getSlope).orElseThrow(),
                result.getSlope(),
                tolerance);
        assertEqualsTolerance(
                ref.map(com.github.dfauth.ta.functions.ref.LinearRegression::getIntercept).orElseThrow(),
                result.getIntercept(),
                tolerance);
        assertEqualsTolerance(
                ref.map(com.github.dfauth.ta.functions.ref.LinearRegression::getR2).orElseThrow(),
                result.getR2(),
                tolerance);
        assertEqualsTolerance(
                ref.map(com.github.dfauth.ta.functions.ref.LinearRegression::getSvar0).orElseThrow(),
                result.getSvar0(),
                tolerance);
        assertEqualsTolerance(
                ref.map(com.github.dfauth.ta.functions.ref.LinearRegression::getSvar1).orElseThrow(),
                result.getSvar1(),
                tolerance);
    }

    private void assertEqualsTolerance(double ref, double result, double tolerance) {
        assertEquals(ref, result, tolerance*ref);
    }
}
