package com.github.dfauth.ta.functions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.dfauth.ta.functional.HistoricalOffset;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.dfauth.ta.functional.HistoricalOffset.Direction.FORWARD;
import static com.github.dfauth.ta.functional.HistoricalOffset.zipWithHistoricalOffset;

public class LinearRegression {

    public static Optional<LineOfBestFit> lobf(List<BigDecimal> y) {
        if (y.isEmpty()) {
            return Optional.empty();
        }

        List<Map.Entry<Double, Double>> points = zipWithHistoricalOffset(y, FORWARD).map(HistoricalOffset::toMapEntry).map(e -> Map.entry(e.getKey().doubleValue(),e.getValue().doubleValue())).collect(Collectors.toList());

        FirstPass firstPass = points.stream().reduce(new FirstPass(),
                (fp, e) -> fp.apply(e.getKey(), e.getValue()),
                FirstPass::apply
        );

        return Optional.of(firstPass
                .secondPass(points)
                .thirdPass(points));
    }

    @Data
    @AllArgsConstructor
    public static class FirstPass {

        private final double sumx;
        private final double sumy;
        private final double sumx2;

        public FirstPass() {
            this(0,0,0);
        }

        public FirstPass apply(double x, double y) {
            return new FirstPass(sumx+x, sumy+y, sumx2+(x*x));
        }

        public FirstPass apply(FirstPass fp) {
            throw new UnsupportedOperationException();
        }

        public SecondPass secondPass(List<Map.Entry<Double, Double>> points) {
            double n = points.size();
            double xbar = sumx/n;
            double ybar = sumy/n;

            // second pass: compute summary statistics
            return points
                    .stream()
                    .reduce(new SecondPass(xbar, ybar),
                            (sp, e) -> sp.apply(e.getKey(), e.getValue()),
                            SecondPass::apply
                    );
        }
    }

    @Data
    @AllArgsConstructor
    public static class SecondPass {

        private final double xbar;
        private final double ybar;
        private final double xxbar;
        private final double yybar;
        private final double xybar;

        public SecondPass(double xbar, double ybar) {
            this(xbar, ybar, 0, 0, 0);
        }

        public SecondPass apply(double x, double y) {
            return new SecondPass(xbar,
                    ybar,
                    bar(xxbar, x, xbar, x, xbar),
                    bar(yybar, y, ybar, y, ybar),
                    bar(xybar, x, xbar, y, ybar)
            );
        }

        private static double bar(double r, double x1, double x2, double y1, double y2) {
            return r + (x1 - x2) * (y1 - y2);
        }

        public SecondPass apply(SecondPass sp) {
            throw new UnsupportedOperationException();
        }

        public LineOfBestFit thirdPass(List<Map.Entry<Double, Double>> points) {

            // more statistical analysis
            SumOfSquares sumOfSquares = points
                    .stream()
                    .reduce(new SumOfSquares(this),
                            (ss, e) -> ss.apply(e.getKey(), e.getValue()),
                            SumOfSquares::apply
                    );

            return sumOfSquares.getLineOfBestFit(points.size());
        }

        public double getSlope() {
            return xybar/xxbar;
        }

        public double getIntercept() {
            return ybar - getSlope()*xbar;
        }
    }

    @AllArgsConstructor
    public static class SumOfSquares {

        private final SecondPass sp;
        private final double ssr;
        private final double rss;

        public SumOfSquares(SecondPass secondPass) {
            this(secondPass, 0,0);
        }

        public SumOfSquares apply(double key, double value) {
            double fit = sp.getSlope() * key + sp.getIntercept();
            double _rss = (fit - value) * (fit - value);
            double _ssr = (fit - sp.getYbar()) * (fit - sp.getYbar());
            return new SumOfSquares(sp, this.ssr + _ssr, this.rss + _rss);
        }

        public SumOfSquares apply(SumOfSquares ss) {
            throw new UnsupportedOperationException();
        }

        public LineOfBestFit getLineOfBestFit(int n) {
            int degreesOfFreedom = n - 2;
            double r2    = ssr / sp.getYybar();
            double svar  = rss / degreesOfFreedom;
            double svar1 = svar / sp.getXxbar();
            double svar0 = svar/n + sp.getXbar()*sp.getXbar()*svar1;

            return new LineOfBestFit(sp.getSlope(),
                    sp.getIntercept(),
                    r2,
                    svar,
                    svar0,
                    svar1
            );
        }
    }

    @AllArgsConstructor
    public static class LineOfBestFit {
        private final double slope;
        private final double intercept;
        private final double r2;
        private final double svar;
        private final double svar0;
        private final double svar1;

        @JsonIgnore
        public BigDecimal getSlopeStdErr() {
            return BigDecimal.valueOf(Math.sqrt(svar1));
        }

        @JsonIgnore
        public BigDecimal getInterceptStdErr() {
            return BigDecimal.valueOf(Math.sqrt(svar0));
        }

        public BigDecimal predict(int i) {
            return BigDecimal.valueOf(slope*i+intercept);
        }

        public double getS() {
            return scale(getSlope(),5);
        }

        public double getV() {
            return scale(getSvar(),5);
        }

        private double scale(double value, int scale) {
            return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP).doubleValue();
        }

        @JsonIgnore
        public double getSlope() {
            return slope;
        }

        @JsonIgnore
        public double getIntercept() {
            return intercept;
        }

        @JsonIgnore
        public double getR2() {
            return r2;
        }

        @JsonIgnore
        public double getSvar() {
            return svar;
        }

        @JsonIgnore
        public double getSvar0() {
            return svar0;
        }

        @JsonIgnore
        public double getSvar1() {
            return svar1;
        }
    }
}
