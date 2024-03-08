package com.github.dfauth.ta.functions.ref;

/******************************************************************************
 *  Compilation:  javac LinearRegression.java
 *  Execution:    java  LinearRegression
 *  Dependencies: none
 *
 *  Compute least squares solution to y = beta * x + alpha.
 *  Simple linear regression.
 *
 ******************************************************************************/


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 *  The {@code LinearRegression} class performs a simple linear regression
 *  on an set of <em>n</em> data points (<em>y<sub>i</sub></em>, <em>x<sub>i</sub></em>).
 *  That is, it fits a straight line <em>y</em> = &alpha; + &beta; <em>x</em>,
 *  (where <em>y</em> is the response variable, <em>x</em> is the predictor variable,
 *  &alpha; is the <em>y-intercept</em>, and &beta; is the <em>slope</em>)
 *  that minimizes the sum of squared residuals of the linear regression model.
 *  It also computes associated statistics, including the coefficient of
 *  determination <em>R</em><sup>2</sup> and the standard deviation of the
 *  estimates for the slope and <em>y</em>-intercept.
 *
 *  @author Robert Sedgewick
 *  @author Kevin Wayne
 */

@AllArgsConstructor
public class LinearRegression {

    private final double slope;
    private final double intercept;
    private final double r2;
    private final double svar;
    private final double svar0;
    private final double svar1;

    public static <T> Optional<LinearRegression> calculate(List<T> y, Function<T,Double> f) {
        double[] array = y.stream().map(f).mapToDouble(Double::doubleValue).toArray();
        return calculate(array);
    }

    public static Optional<LinearRegression> calculate(double[] y) {
        double[] array = IntStream.range(0,y.length).mapToObj(Double::new).mapToDouble(Double::doubleValue).toArray();
        return calculate(array, y);
    }

    /**
     * Performs a linear regression on the data points {@code (y[i], x[i])}.
     *
     * @param  x the values of the predictor variable
     * @param  y the corresponding values of the response variable
     * @throws IllegalArgumentException if the lengths of the two arrays are not equal
     */
    public static Optional<LinearRegression> calculate(double[] x, double[] y) {
        if (x.length != y.length) {
            throw new IllegalArgumentException("array lengths are not equal");
        }
        if(x.length < 2) {
            return Optional.empty();
        }

        int n = x.length;

        // first pass
        double sumx = 0.0, sumx2 = 0.0, sumy = 0.0;
        for (int i = 0; i < n; i++) {
            sumx  += x[i];
            sumx2 += x[i]*x[i];
            sumy  += y[i];
        }
        double xbar = sumx / n;
        double ybar = sumy / n;

        // second pass: compute summary statistics
        double xxbar = 0.0, yybar = 0.0, xybar = 0.0;
        for (int i = 0; i < n; i++) {
            xxbar += (x[i] - xbar) * (x[i] - xbar);
            yybar += (y[i] - ybar) * (y[i] - ybar);
            xybar += (x[i] - xbar) * (y[i] - ybar);
        }
        double slope  = xybar / xxbar;
        double intercept = ybar - slope * xbar;

        // more statistical analysis
        double rss = 0.0;      // residual sum of squares
        double ssr = 0.0;      // regression sum of squares
        for (int i = 0; i < n; i++) {
            double fit = slope*x[i] + intercept;
            rss += (fit - y[i]) * (fit - y[i]);
            ssr += (fit - ybar) * (fit - ybar);
        }

        int degreesOfFreedom = n-2;
        double r2    = ssr / yybar;
        double svar  = rss / degreesOfFreedom;
        double svar1 = svar / xxbar;
        double svar0 = svar/n + xbar*xbar*svar1;

        return Optional.of(new LinearRegression(slope, intercept, r2, svar, svar0, svar1));
    }

    /**
     * Returns the standard error of the estimate for the intercept.
     *
     * @return the standard error of the estimate for the intercept
     */
    public double interceptStdErr() {
        return Math.sqrt(svar0);
    }

    /**
     * Returns the standard error of the estimate for the slope.
     *
     * @return the standard error of the estimate for the slope
     */
    public double slopeStdErr() {
        return Math.sqrt(svar1);
    }

    /**
     * Returns the expected response {@code y} given the value of the predictor
     * variable {@code x}.
     *
     * @param  x the value of the predictor variable
     * @return the expected response {@code y} given the value of the predictor
     *         variable {@code x}
     */
    public double predict(double x) {
        return slope*x + intercept;
    }

    /**
     * Returns a string representation of the simple linear regression model.
     *
     * @return a string representation of the simple linear regression model,
     *         including the best-fit line and the coefficient of determination
     *         <em>R</em><sup>2</sup>
     */
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(String.format("%.2f n + %.2f", slope, intercept));
        s.append("  (R^2 = " + String.format("%.3f", r2) + ")");
        return s.toString();
    }

    public double getS() {
        return scale(slope,5);
    }

    public double getV() {
        return scale(svar,5);
    }

    private double scale(double v, int i) {
        return BigDecimal.valueOf(v).setScale(i, RoundingMode.HALF_UP).doubleValue();
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