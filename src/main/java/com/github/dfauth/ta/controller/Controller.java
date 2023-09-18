package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.functions.Accumulator;
import com.github.dfauth.ta.functions.MovingAverages;
import com.github.dfauth.ta.functions.RateOfChange;
import com.github.dfauth.ta.model.*;
import com.github.dfauth.ta.repo.PriceRepository;
import com.github.dfauth.ta.repo.TradeRepository;
import com.github.dfauth.ta.repo.ValuationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.dfauth.ta.functional.Lists.mapList;
import static com.github.dfauth.ta.functions.RSI.calculateRSI;
import static com.github.dfauth.ta.functions.Reducers.latest;
import static com.github.dfauth.ta.model.Price.parseDate;
import static com.github.dfauth.ta.model.Price.parsePrice;

@RestController
@Slf4j
public class Controller {

    @Autowired
    private PriceRepository repository;

    @Autowired
    private ValuationRepository valuationRepo;

    @Autowired
    private TradeRepository tradeRepo;

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

    @PostMapping("/sync/valuations/{_date}")
    @ResponseStatus(HttpStatus.CREATED)
    Integer valuations(@PathVariable Integer _date, @RequestBody Object[][] args) {
        log.error("args: ",args);
        LocalDateTime timestamp = LocalDateTime.of(LocalDate.of(_date / 100, _date % 100, 1), LocalTime.of(0,0));
        List<Valuation> valuations = Stream.of(args).filter(arr -> toCode(arr[0]).length() > 0).map(arr -> {
            return new Valuation(
                    toCode(arr[0]),
                    new Timestamp(timestamp.toInstant(ZoneOffset.UTC).toEpochMilli()),
                    Rating.fromString((String)arr[4]),
                    (Integer) arr[5],
                    (Integer) arr[6],
                    (Integer) arr[7],
                    toBigDecimal(arr[8])
            );
        }).collect(Collectors.toList());
        valuationRepo.saveAll(valuations);
        return valuations.size();
    }

    @PostMapping("/sync/trades")
    @ResponseStatus(HttpStatus.CREATED)
    Integer trades(@RequestBody Object[][] args) {
        log.error("args: ",args);

        List<Trade> trades = Stream.of(args).map(arr -> new Trade(
            arr[18].toString(),
            new Timestamp(LocalDate.parse((String)arr[0],dtf).atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()),
            (String)arr[1],
            (Integer)arr[2],
            toBigDecimal(arr[3]),
            toBigDecimal(arr[4]),
            Side.fromString(arr[5]),
            arr[14].toString())
        ).collect(Collectors.toList());
        tradeRepo.saveAll(trades);
        return trades.size();
    }

    private static BigDecimal toBigDecimal(Object o) {
        if(o == null) {
            return null;
        } else if(o instanceof Integer) {
            return BigDecimal.valueOf((Integer)o);
        } else if(o instanceof Double){
            return BigDecimal.valueOf((Double) o);
        } else if(o instanceof String){
            if("".equals(o)) {
                return null;
            } else {
                return new BigDecimal((String) o);
            }
        } else {
            throw new IllegalArgumentException("Unsupported type: "+o.getClass());
        }
    }

    private static String toCode(Object o) {
        if(o instanceof Integer) {
            return "ASX:"+ o;
        } else if(o instanceof String){
            String s = (String) o;
            if(s.length() > 0) {
                return "ASX:"+ s;
            } else {
                return s;
            }
        } else {
            throw new IllegalArgumentException("Unsupported type: "+o.getClass());
        }
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
        return prices(_code, period+2).stream()
                .map(Price::get_close)
                .map(RateOfChange.roc())
                .flatMap(Optional::stream)
                .map(MovingAverages.sma(period))
                .flatMap(Optional::stream)
                .reduce(latest());
    }

    // rsi
    @GetMapping("/prices/{_code}/rsi")
    @ResponseStatus(HttpStatus.OK)
    Optional<BigDecimal> rsi(@PathVariable String _code) {
        return rsi(_code,14);
    }

    @GetMapping("/prices/{_code}/rsi/{period}")
    @ResponseStatus(HttpStatus.OK)
    Optional<BigDecimal> rsi(@PathVariable String _code, @PathVariable int period) {
        return calculateRSI(mapList(prices(_code, period+2), Price::get_close));
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
        return price(_code).map(Price::get_close);
    }

    @GetMapping("/price/{_code}/volume")
    @ResponseStatus(HttpStatus.OK)
    Optional<Integer> volume(@PathVariable String _code) {
        return price(_code).map(Price::get_volume);
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

    @GetMapping("/price/{_code}")
    @ResponseStatus(HttpStatus.OK)
    Optional<Price> price(@PathVariable String _code) {
        log.info("price/{}",_code);
        return repository.findLatestByCode(_code);
    }

}
