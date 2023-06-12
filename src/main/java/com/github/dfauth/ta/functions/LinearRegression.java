package com.github.dfauth.ta.functions;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.github.dfauth.ta.functional.FunctionUtils.nullZero;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;
import static java.util.function.Predicate.not;

public class LinearRegression {

    public static double[][] twoDArray(List<Double> l) {

        return IntStream.range(0,l.size())
                .mapToObj(i -> Map.entry(i, l.get(i)))
                .reduce(
                    new double[2][l.size()],
                        (arr, e) -> {
                            arr[0][e.getKey()] = e.getKey();
                            arr[1][e.getKey()] = e.getValue();
                            return arr;
                        },
                        (arr1, arr2) -> {
                            throw new UnsupportedOperationException("Oops");
                        }
        );
    }

    public static Optional<LineOfBestFit> lobf(List<BigDecimal> y) {
        if (y.size() == 0) {
            Optional.empty();
        }

        // first pass
        List<Map.Entry<Integer, BigDecimal>> points = IntStream.range(0,y.size()).mapToObj(i -> Map.entry(i, y.get(i))).collect(Collectors.toList());
        FirstPass firstPass = points
                .stream()
                .reduce(new FirstPass(),
                        (fp, e) -> fp.apply(e.getKey(), e.getValue()),
                        FirstPass::apply
                );

        return firstPass.secondPass(points);
    }

    @Data
    @AllArgsConstructor
    public static class FirstPass {

        private final int sumx;
        private final BigDecimal sumy;
        private final int sumx2;

        public FirstPass() {
            this(0,ZERO,0);
        }

        public FirstPass apply(Integer x, BigDecimal y) {
            return new FirstPass(sumx+x, sumy.add(y), sumx2+(x*x));
        }

        public FirstPass apply(FirstPass fp) {
            throw new UnsupportedOperationException();
        }

        public Optional<LineOfBestFit> secondPass(List<Map.Entry<Integer, BigDecimal>> points) {
            BigDecimal n = BigDecimal.valueOf(points.size());
            BigDecimal xbar = BigDecimal.valueOf(sumx).divide(n, HALF_UP);
            BigDecimal ybar = sumy.divide(n, HALF_UP);

            // second pass: compute summary statistics
            SecondPass secondPass = points
                    .stream()
                    .reduce(new SecondPass(xbar, ybar),
                            (sp, e) -> sp.apply(e.getKey(), e.getValue()),
                            SecondPass::apply
                    );

            return secondPass.thirdPass(points);
        }
    }

    @Data
    @AllArgsConstructor
    public static class SecondPass {

        private final BigDecimal xbar;
        private final BigDecimal ybar;
        private final BigDecimal xxbar;
        private final BigDecimal yybar;
        private final BigDecimal xybar;

        public SecondPass(BigDecimal xbar, BigDecimal ybar) {
            this(xbar, ybar, ZERO, ZERO, ZERO);
        }

        public SecondPass apply(Integer x, BigDecimal y) {
            BigDecimal _x = BigDecimal.valueOf(x);
            return new SecondPass(xbar,
                    ybar,
                    bar(xxbar, _x, xbar, _x, xbar),
                    bar(yybar, y, ybar, y, ybar),
                    bar(xybar, _x, xbar, y, ybar)
            );
        }

        private static BigDecimal bar(BigDecimal r, BigDecimal x1, BigDecimal x2, BigDecimal y1, BigDecimal y2) {
//            r + (x1 - y1) * (x2 - y2);
            return x1.subtract(x2).multiply(y1.subtract(y2)).add(r);
        }

        public SecondPass apply(SecondPass sp) {
            throw new UnsupportedOperationException();
        }

        public Optional<LineOfBestFit> thirdPass(List<Map.Entry<Integer, BigDecimal>> points) {

            // more statistical analysis
            SumOfSquares sumOfSquares = points
                    .stream()
                    .reduce(new SumOfSquares(this),
                            (ss, e) -> ss.apply(e.getKey(), e.getValue()).orElse(new SumOfSquares(this)),
                            SumOfSquares::apply
                    );

            return sumOfSquares.getLineOfBestFit(points.size());
        }

        public Optional<BigDecimal> getSlope() {
            return Optional.of(xxbar).filter(not(ZERO::equals)).map(_xxbar -> xybar.divide(_xxbar, HALF_UP));
        }

        public Optional<BigDecimal> getIntercept() {
            return getSlope().map(_slope -> ybar.subtract(_slope.multiply(xbar)));
        }
    }

    @Data
    @AllArgsConstructor
    public static class SumOfSquares {

        private final SecondPass sp;
        private final BigDecimal ssr;
        private final BigDecimal rss;

        public SumOfSquares(SecondPass secondPass) {
            this(secondPass, ZERO,ZERO);
        }

        public Optional<SumOfSquares> apply(Integer key, BigDecimal value) {
            Optional<BigDecimal> fit = sp.getSlope().flatMap(_slope -> sp.getIntercept().map(_intercept -> _slope.multiply(BigDecimal.valueOf(key)).add(_intercept)));
            return fit.map(_fit -> new SumOfSquares(sp, SecondPass.bar(rss, _fit, value, _fit, value), SecondPass.bar(ssr, _fit, sp.getYbar(), _fit, sp.getYbar())));
        }

        public SumOfSquares apply(SumOfSquares ss) {
            throw new UnsupportedOperationException();
        }

        public Optional<LineOfBestFit> getLineOfBestFit(int n) {
            BigDecimal degreesOfFreedom = BigDecimal.valueOf(n - 2);
            Optional<BigDecimal> r2 = nullZero(_nz -> ssr.divide(_nz, HALF_UP)).apply(sp.getYybar());
            Optional<BigDecimal> svar  = nullZero(_df -> rss.divide(_df, HALF_UP)).apply(degreesOfFreedom);

            return svar.flatMap(_svar -> {
                Optional<BigDecimal> svar1 = nullZero(_xxbar -> _svar.divide(_xxbar, HALF_UP)).apply(sp.getXxbar());
                return svar1.flatMap(_svar1 -> {
                    BigDecimal svar0 = _svar.divide(BigDecimal.valueOf(n),HALF_UP).add(sp.getXbar().multiply(sp.getXbar()).multiply(_svar1));
                    return r2.flatMap(_r2 -> sp.getSlope()
                            .flatMap(_slope -> sp.getIntercept()
                                    .map(_intercept -> new LineOfBestFit(_slope,
                                                    _intercept,
                                                    _r2,
                                                    _svar,
                                                    svar0,
                                                    _svar1
                                            )
                                    )
                            )
                    );
                });
            });
        }
    }

    @Data
    @AllArgsConstructor
    public static class LineOfBestFit {
        private final BigDecimal slope;
        private final BigDecimal intercept;
        private final BigDecimal r2;
        private final BigDecimal svar;
        private final BigDecimal svar0;
        private final BigDecimal svar1;

        public BigDecimal getSlopeStdErr() {
            return BigDecimal.valueOf(Math.sqrt(svar1.doubleValue()));
        }

        public BigDecimal getInterceptStdErr() {
            return BigDecimal.valueOf(Math.sqrt(svar0.doubleValue()));
        }

        public BigDecimal predict(int i) {
            return slope.multiply(BigDecimal.valueOf(i)).add(intercept);
        }
    }
}
