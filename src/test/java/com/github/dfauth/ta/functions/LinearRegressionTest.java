package com.github.dfauth.ta.functions;

import com.github.dfauth.ta.model.Price;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.dfauth.ta.functional.Lists.mapList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class LinearRegressionTest {

    private double tolerance = 0.03d;

    @Test
    public void testIt() {
        Optional<LinearRegression.LineOfBestFit> result = LinearRegression.lobf(List.of(0, 1, 2, 3, 4, 5).stream().map(BigDecimal::new).collect(Collectors.toList()));
        assertEqualsTolerance(1.0d, result.get().getSlope(), tolerance);
        assertEqualsTolerance(0.0d, result.get().getIntercept(), tolerance);
        assertEqualsTolerance(1.0d, result.get().getR2(), tolerance);
        assertEqualsTolerance(0.0d, result.get().getInterceptStdErr().doubleValue(), tolerance);
        assertEqualsTolerance(0.0d, result.get().getSlopeStdErr().doubleValue(), tolerance);
    }

    @Test
    public void testMP1() {
        Optional<LinearRegression.LineOfBestFit> result = LinearRegression.lobf(mapList(TestData.MP1, Price::getClose));
        assertEqualsTolerance(0.0187195918003124d, result.get().getSlope(), tolerance);
        assertEqualsTolerance(4.849619800489363d, result.get().getIntercept(), tolerance);
        assertEqualsTolerance(0.3663877437030541d, result.get().getR2(), tolerance);
        assertEqualsTolerance(0.2258462657200783d, result.get().getInterceptStdErr().doubleValue(), tolerance);
        assertEqualsTolerance(0.0015569249774101891d, result.get().getSlopeStdErr().doubleValue(), tolerance);
    }

    private void assertEqualsTolerance(double ref, double result, double tolerance) {
        assertEquals(ref, result, tolerance*ref);
    }
}
