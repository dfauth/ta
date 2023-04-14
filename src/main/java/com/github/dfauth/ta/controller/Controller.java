package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.functions.MovingAverages;
import com.github.dfauth.ta.functions.RateOfChange;
import com.github.dfauth.ta.model.Price;
import com.github.dfauth.ta.repo.PriceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.dfauth.ta.model.Price.parseDate;
import static com.github.dfauth.ta.model.Price.parsePrice;

@RestController
@Slf4j
public class Controller {

    @Autowired
    private PriceRepository repository;

    // Save
    @PostMapping("/sync/{_code}")
    @ResponseStatus(HttpStatus.CREATED)
    Integer sync(@PathVariable String _code, @RequestBody Object[][] args) {
        List<Price> prices = Stream.of(args)
                .map(a -> new Price(_code, parseDate((String) a[0]), parsePrice(a[1]), parsePrice(a[2]), parsePrice(a[3]), parsePrice(a[4]), (Integer) a[5]))
                .filter(p -> repository.findById(p.getKey()).isEmpty())
                .collect(Collectors.toList());
        repository.saveAll(prices);
        return prices.size();
    }

    @GetMapping("/prices/{_code}/sma/{period}")
    @ResponseStatus(HttpStatus.OK)
    Optional<BigDecimal> sma(@PathVariable String _code, @PathVariable int period) {
        log.info("sma/{}/{}",_code,period);
        Function<BigDecimal, Optional<BigDecimal>> f = MovingAverages.sma(period);
        return prices(_code, period+2).stream()
                .map(Price::get_close)
                .map(f)
                .flatMap(Optional::stream)
                .reduce((t1,t2) -> t2);
    }

    @GetMapping("/prices/{_code}/roc")
    @ResponseStatus(HttpStatus.OK)
    Optional<BigDecimal> roc(@PathVariable String _code) {
        return roc(_code,1);
    }

    @GetMapping("/prices/{_code}/roc/{period}")
    @ResponseStatus(HttpStatus.OK)
    Optional<BigDecimal> roc(@PathVariable String _code, @PathVariable int period) {
        return prices(_code, period+2).stream()
                .map(Price::get_close)
                .map(RateOfChange.roc())
                .flatMap(Optional::stream)
                .map(MovingAverages.sma(period))
                .flatMap(Optional::stream)
                .reduce((t1,t2) -> t2);
    }

    @GetMapping("/price/{_code}/roc/{period}")
    @ResponseStatus(HttpStatus.OK)
    List<Price> prices(@PathVariable String _code, int period) {
        return repository.findByCode(_code, period);
    }

    @GetMapping("/price/{_code}/close")
    @ResponseStatus(HttpStatus.OK)
    Optional<BigDecimal> close(@PathVariable String _code) {
        return price(_code).map(Price::get_close);
    }

    @GetMapping("/price/{_code}/volume")
    @ResponseStatus(HttpStatus.OK)
    Optional<Integer> volume(@PathVariable String _code) {
        return price(_code).map(Price::get_volume);
    }

    @GetMapping("/price/{_code}")
    @ResponseStatus(HttpStatus.OK)
    Optional<Price> price(@PathVariable String _code) {
        log.info("price/{}",_code);
        return repository.findLatestByCode(_code);
    }

}
