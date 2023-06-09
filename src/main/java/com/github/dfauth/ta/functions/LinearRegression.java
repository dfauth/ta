package com.github.dfauth.ta.functions;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.github.dfauth.ta.functional.FunctionUtils.nullZero;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;
import static java.util.function.Predicate.not;

public class LinearRegression {

    public static Optional<LineOfBestFit> lobf(List<BigDecimal> y) {
        if (y.size() == 0) {
            Optional.empty();
        }

        // first pass
        AtomicInteger i = new AtomicInteger();
        List<Map.Entry<Integer, BigDecimal>> points = y.stream().map(_y -> Map.entry(i.getAndIncrement(), _y)).collect(Collectors.toList());
        FirstPass firstPass = points.stream().reduce(new FirstPass(), (fp, e) -> fp.apply(e.getKey(), e.getValue()), FirstPass::apply);

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

        public FirstPass apply(Integer key, BigDecimal value) {
            return new FirstPass(sumx+key, sumy.add(value), sumx2+key*key);
        }

        public FirstPass apply(FirstPass fp) {
            return new FirstPass(sumx+fp.sumx, sumy.add(fp.sumy), sumx2+fp.sumx2);
        }

        public Optional<LineOfBestFit> secondPass(List<Map.Entry<Integer, BigDecimal>> points) {
            BigDecimal n = BigDecimal.valueOf(points.size());
            BigDecimal xbar = BigDecimal.valueOf(sumx).divide(n, HALF_UP);
            BigDecimal ybar = sumy.divide(n, HALF_UP);

            // second pass: compute summary statistics
            SecondPass secondPass = points.stream().reduce(new SecondPass(xbar, ybar), (sp, e) -> sp.apply(e.getKey(), e.getValue()), SecondPass::apply);

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

        public SecondPass apply(Integer key, BigDecimal value) {
            return new SecondPass(xbar,
                    ybar,
                    bar(xxbar, BigDecimal.valueOf(key), xbar),
                    bar(yybar, value, ybar),
                    bar(xybar, BigDecimal.valueOf(key), xbar, value, ybar)
            );
        }

        private static BigDecimal bar(BigDecimal r, BigDecimal x, BigDecimal y) {
            return bar(r,x,x,y,y);
        }

        private static BigDecimal bar(BigDecimal r, BigDecimal x1, BigDecimal x2, BigDecimal y1, BigDecimal y2) {
//            r + (x1 - y1) * (x2 - y2);
            return x1.subtract(y1).multiply(x2.subtract(y2)).add(r);
        }

        public SecondPass apply(SecondPass sp) {
            return new SecondPass(xbar, ybar, xxbar.add(sp.xxbar),yybar.add(sp.yybar),xybar.add(sp.xybar));
        }

        public Optional<LineOfBestFit> thirdPass(List<Map.Entry<Integer, BigDecimal>> points) {

            // more statistical analysis
            SumOfSquares sumOfSquares = points.stream().reduce(new SumOfSquares(this), (ss, e) -> ss.apply(e.getKey(), e.getValue()).orElse(new SumOfSquares(this)), SumOfSquares::apply);

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
            return fit.map(_fit -> new SumOfSquares(sp, SecondPass.bar(rss, _fit, value), SecondPass.bar(ssr, _fit, sp.getYbar())));
        }

        public SumOfSquares apply(SumOfSquares ss) {
            return new SumOfSquares(sp, ssr.add(ss.ssr), rss.add(ss.rss));
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
