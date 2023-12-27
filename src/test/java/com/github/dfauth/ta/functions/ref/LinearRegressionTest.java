package com.github.dfauth.ta.functions.ref;

import com.github.dfauth.ta.functions.TestData;
import com.github.dfauth.ta.model.Price;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.github.dfauth.ta.functional.Lists.mapList;
import static com.github.dfauth.ta.functions.ref.LinearRegression.calculate;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
    public void testMP1() {
        Optional<LinearRegression> result = calculate(mapList(TestData.MP1, Price::getClose), BigDecimal::doubleValue);
        assertEquals(0.0187195918003124d, result.map(LinearRegression::getSlope).orElseThrow(), 0.01d);
        assertEquals(4.849619800489363d, result.map(LinearRegression::getIntercept).orElseThrow(), 0.01d);
        assertEquals(0.3663877437030541d, result.map(LinearRegression::getR2).orElseThrow(), 0.01d);
        assertEquals(0.2258462657200783d, result.map(LinearRegression::interceptStdErr).orElseThrow(), 0.01d);
        assertEquals(0.0015569249774101891d, result.map(LinearRegression::slopeStdErr).orElseThrow(), 0.00001d);
    }
}
