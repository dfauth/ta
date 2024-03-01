package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.functional.Lists;
import com.github.dfauth.ta.functional.Valuations;
import com.github.dfauth.ta.model.Rating;
import com.github.dfauth.ta.model.Valuation;
import com.github.dfauth.ta.repo.ValuationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.dfauth.ta.controller.ControllerUtils.toBigDecimal;
import static com.github.dfauth.ta.functional.Lists.last;

@RestController
@Slf4j
public class ValuationController {

    @Autowired
    private ValuationRepository repository;

    List<Valuation> valuations(@PathVariable String _code, int period) {
        return repository.findByCode(_code, period);
    }

    @GetMapping("/valuation/{_code}")
    @ResponseStatus(HttpStatus.OK)
    List<Valuation> valuationHistory(@PathVariable String _code) {
        log.info("valuation history code: {}",_code);
        List<Valuation> result = repository.findByCode(_code);
        result.sort(Valuation.sortByDate);
        return result;
    }

    @GetMapping("/valuation/{_code}/target/price")
    @ResponseStatus(HttpStatus.OK)
    Map<LocalDate,BigDecimal> valuationHistoryTargetPrice(@PathVariable String _code) {
        log.info("valuationHistoryTargetPrice code: {}",_code);
        List<Valuation> result = valuationHistory(_code);
        return Lists.toMap(result, v -> v.get_date().toLocalDateTime().toLocalDate(),Valuation::getTarget);
    }

    @GetMapping("/valuation/{_code}/change/{period}")
    @ResponseStatus(HttpStatus.OK)
    Optional<BigDecimal> valuationChange(@PathVariable String _code, @PathVariable int period) {
        log.info("valuation/{}/change/{}",_code,period);
        return last(Valuations.valuationChange(valuations(_code, period)));
    }

    @PostMapping("/sync/valuations/{_date}")
    @ResponseStatus(HttpStatus.CREATED)
    Integer valuations(@PathVariable Integer _date, @RequestBody Object[][] args) {
        log.error("args: ",args);
        Timestamp timestamp = new Timestamp(LocalDateTime.of(LocalDate.of(_date / 100, _date % 100, 1), LocalTime.of(0, 0)).toInstant(ZoneOffset.UTC).toEpochMilli());
        List<Valuation> valuations = Stream.of(args).filter(arr -> !toCode(arr[0]).isEmpty()).map(arr -> {
            return new Valuation(
                    toCode(arr[0]),
                    timestamp,
                    Rating.fromString((String)arr[4]),
                    (Integer) arr[5],
                    (Integer) arr[6],
                    (Integer) arr[7],
                    toBigDecimal(arr[8])
            );
        }).collect(Collectors.toList());
        repository.saveAll(valuations);
        return valuations.size();
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

}
