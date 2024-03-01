package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.functions.ref.LinearRegression;
import com.github.dfauth.ta.model.Price;
import com.github.dfauth.ta.repo.PriceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.dfauth.ta.functional.FunctionUtils.windowfy;
import static com.github.dfauth.ta.functional.Lists.mapList;
import static com.github.dfauth.ta.functions.Reducers.latest;
import static java.math.RoundingMode.HALF_UP;

@RestController
@Slf4j
public class LinearRegressionController implements ControllerMixIn {

    @Autowired
    private PriceRepository repository;

    // lobf
    @PostMapping("/lobf/slope/{period}")
    @ResponseStatus(HttpStatus.OK)
    Map<String, BigDecimal> lobf(@RequestBody List<List<String>> codes, @PathVariable int period) {
        log.info("lobf/slope/{}/{}",codes,period);
        return flatMapCode(codes, code -> lobf(code, period)
                .stream());
    }

    @GetMapping("/lobf/slope/{_code}/{period}")
    @ResponseStatus(HttpStatus.OK)
    Optional<BigDecimal> lobf(@PathVariable String _code, @PathVariable int period) {
        try {
            log.info("lobf/slope/{}/{}",_code,period);
            List<Price> prices = prices(_code, period);
            Optional<LinearRegression> result = linearRegression(prices);
            return result.map(LinearRegression::getSlope).map(BigDecimal::valueOf).map(bd -> bd.divide(prices.get(prices.size()-1).get_close(), HALF_UP));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Optional.empty();
        }
    }

    @GetMapping("/linearRegression/{_code}/{period}")
    @ResponseStatus(HttpStatus.OK)
    Optional<com.github.dfauth.ta.functions.LinearRegression.LineOfBestFit> linearRegression(@PathVariable String _code, @PathVariable int period) {
        log.info("linearRegression/slope/{}/{}",_code,period);
        List<Price> prices = prices(_code, period);
        return com.github.dfauth.ta.functions.LinearRegression.lobf(mapList(prices, Price::getClose));
    }

    public static Optional<LinearRegression> linearRegression(List<Price> prices) {
        Function<List<BigDecimal>, Optional<LinearRegression>> f = l -> LinearRegression.calculate(l, BigDecimal::doubleValue);
        return f.apply(prices.stream().map(Price::get_close).collect(Collectors.toList()));
    }

    private Optional<BigDecimal> closeOperation(String _code, int period, Function<List<BigDecimal>, Optional<BigDecimal>> f1) {
        Function<BigDecimal, Optional<BigDecimal>> f = windowfy(period, f1);
        return prices(_code, period).stream()
                .map(Price::get_close)
                .map(f)
                .flatMap(Optional::stream)
                .reduce(latest());
    }

    List<Price> prices(@PathVariable String _code, int period) {
        return repository.findByCode(_code, period);
    }

}
