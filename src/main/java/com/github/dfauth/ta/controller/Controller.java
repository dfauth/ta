package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.functions.Accumulator;
import com.github.dfauth.ta.functions.MovingAverages;
import com.github.dfauth.ta.functions.RateOfChange;
import com.github.dfauth.ta.model.Price;
import com.github.dfauth.ta.model.PriceAction;
import com.github.dfauth.ta.repo.PriceRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.dfauth.ta.functions.Reducers.latest;
import static com.github.dfauth.ta.model.Price.parseDate;
import static com.github.dfauth.ta.model.Price.parsePrice;
import static io.github.dfauth.trycatch.ExceptionalRunnable.tryCatch;
import static java.util.Optional.empty;

@RestController
@Slf4j
public class Controller implements ControllerMixIn {

    @Autowired
    private PriceRepository repository;

    private DateTimeFormatter dtf = DateTimeFormatter.ISO_DATE_TIME;

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

    // sma
    @GetMapping("/prices/{_code}/sma/{period}")
    @ResponseStatus(HttpStatus.OK)
    Optional<BigDecimal> sma(@PathVariable String _code, @PathVariable int period) {
        log.info("sma/{}/{}",_code,period);
        Function<BigDecimal, Optional<BigDecimal>> f = MovingAverages.sma(period);
        return prices(_code, period+2).stream()
                .map(Price::get_close)
                .map(f)
                .flatMap(Optional::stream)
                .reduce(latest());
    }

    // ema
    @GetMapping("/prices/{_code}/ema/{period}")
    @ResponseStatus(HttpStatus.OK)
    Optional<BigDecimal> ema(@PathVariable String _code, @PathVariable int period) {
        log.info("ema/{}/{}",_code,period);
        Function<BigDecimal, Optional<BigDecimal>> f = MovingAverages.ema(period);
        return prices(_code, period+2).stream()
                .map(Price::get_close)
                .map(f)
                .flatMap(Optional::stream)
                .reduce(latest());
    }

    // roc
    @GetMapping("/prices/{_code}/roc")
    @ResponseStatus(HttpStatus.OK)
    Optional<BigDecimal> roc(@PathVariable String _code) {
        return roc(_code,1);
    }

    @GetMapping("/prices/{_code}/roc/{period}")
    @ResponseStatus(HttpStatus.OK)
    Optional<BigDecimal> roc(@PathVariable String _code, @PathVariable int period) {
        return tryCatch(() -> prices(_code, period+2).stream()
                .map(Price::get_close)
                .map(RateOfChange.roc())
                .flatMap(Optional::stream)
                .map(MovingAverages.sma(period))
                .flatMap(Optional::stream)
                .reduce(latest()), e -> {
            log.error("exception when processing roc request for code {} period {} message: {}",_code,period,e.getMessage(),e);
            throw new RuntimeException(e);
        });
    }

    @GetMapping("/price/{_code}/roc/{period}")
    @ResponseStatus(HttpStatus.OK)
    List<Price> prices(@PathVariable String _code, int period) {
        return repository.findByCode(_code, period);
    }

    // prices
    @GetMapping("/price/{_code}/close")
    @ResponseStatus(HttpStatus.OK)
    Optional<BigDecimal> close(@PathVariable String _code) {
        return price(_code).map(OHLC::getClose);
    }

    @GetMapping("/price/{_code}/volume")
    @ResponseStatus(HttpStatus.OK)
    Optional<Integer> volume(@PathVariable String _code) {
        return price(_code).map(OHLC::getVol);
    }

    @GetMapping("/prices/{_code}/volume/sma/{_period}")
    @ResponseStatus(HttpStatus.OK)
    Optional<Double> volume(@PathVariable String _code, @PathVariable Integer _period) {
        return prices(_code,_period+1)
                .stream()
                .map(Price::get_volume)
                .map(MovingAverages.sma(_period, Accumulator.INT_ACCUMULATOR.get()))
                .flatMap(Optional::stream)
                .reduce(latest());
    }

    @PostMapping("/price")
    @ResponseStatus(HttpStatus.OK)
    Map<String, OHLC> price(@RequestBody List<List<String>> codes) {
        try {
            log.info("price/{}",codes);
            Map<String, OHLC> result = flatMapCode(codes, code -> price(code).stream());
            return result;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/price/{_code}")
    @ResponseStatus(HttpStatus.OK)
    Optional<OHLC> price(@PathVariable String _code) {
        try {
            log.info("price/{}",_code);
            return repository.findLatestByCode(_code).map(OHLC::new);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return empty();
        }
    }

    @Data
    @AllArgsConstructor
    public static class OHLC {

        private final BigDecimal open;
        private final BigDecimal hi;
        private final BigDecimal lo;
        private final BigDecimal close;
        private final int vol;

        public OHLC(PriceAction priceAction) {
            this(priceAction.getOpen(),
                 priceAction.getHigh(),
                 priceAction.getLow(),
                 priceAction.getClose(),
                 priceAction.getVolume());
        }
    }

}
