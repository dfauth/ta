package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.functions.ref.LinearRegression;
import com.github.dfauth.ta.model.Price;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.dfauth.ta.functional.Lists.mapList;
import static java.math.RoundingMode.HALF_UP;
import static java.util.Optional.empty;

@RestController
@Slf4j
public class LinearRegressionController extends PriceController implements ControllerMixIn {

    // lobf
    @PostMapping("/lobf/{period}")
    @ResponseStatus(HttpStatus.OK)
    Map<String, LinearRegression> lobf(@RequestBody List<List<String>> codes, @PathVariable int period) {
        log.info("lobf/{}/{}",codes,period);
        return flatMapCode(codes, code -> lobf(code, period)
                .stream());
    }

    @GetMapping("/lobf/{_code}/{period}")
    @ResponseStatus(HttpStatus.OK)
    Optional<LinearRegression> lobf(@PathVariable String _code, @PathVariable int period) {
        try {
            log.info("lobf/{}/{}",_code,period);
            List<Price> prices = prices(_code, period);
            Optional<LinearRegression> result = linearRegression(prices);
            return result;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return empty();
        }
    }

    @PostMapping("/lobf/slope/{period}")
    @ResponseStatus(HttpStatus.OK)
    Map<String, BigDecimal> lobfSlope(@RequestBody List<List<String>> codes, @PathVariable int period) {
        log.info("lobf/slope/{}/{}",codes,period);
        return flatMapCode(codes, code -> lobfSlope(code, period)
                .stream());
    }

    @GetMapping("/lobf/slope/{_code}/{period}")
    @ResponseStatus(HttpStatus.OK)
    Optional<BigDecimal> lobfSlope(@PathVariable String _code, @PathVariable int period) {
        try {
            log.info("lobf/slope/{}/{}",_code,period);
            List<Price> prices = prices(_code, period);
            Optional<LinearRegression> result = linearRegression(prices);
            return result.map(LinearRegression::getSlope).map(BigDecimal::valueOf).map(bd -> bd.divide(prices.get(prices.size()-1).getClose(), HALF_UP));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return empty();
        }
    }

    @PostMapping("/lr/{period}")
    @ResponseStatus(HttpStatus.OK)
    Map<String, com.github.dfauth.ta.functions.LinearRegression.LineOfBestFit> linearRegression(@RequestBody List<List<String>> codes, @PathVariable int period) {
        try {
            log.info("lr/{}/{}",codes,period);
            Timestamp marketDate = latestPriceDate();
            return flatMapCode(codes, code -> linearRegression(code, marketDate, period).stream());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/lr/{_code}/{period}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<com.github.dfauth.ta.functions.LinearRegression.LineOfBestFit> linearRegression(@PathVariable String _code, @PathVariable int period) {
        try {
            log.info("lr/{}/{}",_code,period);
            return linearRegression(_code, latestPriceDate(), period);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return empty();
        }
    }

    private Optional<com.github.dfauth.ta.functions.LinearRegression.LineOfBestFit> linearRegression(String _code, Timestamp marketDate, int period) {
        try {
            List<Price> prices = prices(_code, period);
            return com.github.dfauth.ta.functions.LinearRegression.lobf(mapList(prices, Price::getClose));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return empty();
        }
    }

    public static Optional<LinearRegression> linearRegression(List<Price> prices) {
        Function<List<BigDecimal>, Optional<LinearRegression>> f = l -> LinearRegression.calculate(l, BigDecimal::doubleValue);
        return f.apply(prices.stream().map(Price::getClose).collect(Collectors.toList()));
    }

}
