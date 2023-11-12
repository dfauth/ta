package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.functional.Valuations;
import com.github.dfauth.ta.model.Valuation;
import com.github.dfauth.ta.repo.ValuationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.github.dfauth.ta.functional.Lists.last;

@RestController
@Slf4j
public class ValuationController {

    @Autowired
    private ValuationRepository repository;

    List<Valuation> valuations(@PathVariable String _code, int period) {
        return repository.findByCode(_code, period);
    }

    @GetMapping("/valuation/{_code}/change/{period}")
    @ResponseStatus(HttpStatus.OK)
    Optional<BigDecimal> valuationChange(@PathVariable String _code, @PathVariable int period) {
        log.info("valuation/{}/change/{}",_code,period);
        return last(Valuations.valuationChange(valuations(_code, period)));
    }
}
