package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.functions.Accumulator;
import com.github.dfauth.ta.functions.MovingAverages;
import com.github.dfauth.ta.repo.PriceRepository;
import com.github.dfauth.ta.model.Price;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
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

    @GetMapping("/sma/{_code}/{period}")
    @ResponseStatus(HttpStatus.OK)
    BigDecimal sma(@PathVariable String _code, @PathVariable int period) {
        log.info("sma/{}/{}",_code,period);
        List<BigDecimal> l = repository.findByCode(_code).stream().map(Price::get_close).map(MovingAverages.sma(period, Accumulator.BD_ACCUMULATOR)).collect(Collectors.toList());
        return l.get(l.size()-1);
    }

    @GetMapping("/price/{_code}")
    @ResponseStatus(HttpStatus.OK)
    Optional<BigDecimal> price(@PathVariable String _code) {
        log.info("price/{}",_code);
        return repository.findLatestByCode(_code).map(Price::get_close);
    }

}
