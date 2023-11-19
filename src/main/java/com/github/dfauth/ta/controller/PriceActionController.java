package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.functional.PriceActionFunctions;
import com.github.dfauth.ta.model.PriceAction;
import com.github.dfauth.ta.repo.PriceRepository;
import com.github.dfauth.ta.util.CalculatingRingBuffer;
import com.github.dfauth.ta.util.RingBuffer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@Slf4j
public class PriceActionController {

    @Autowired
    private PriceRepository repository;

    @GetMapping("/price/action/{code}/{periods}")
    @ResponseStatus(HttpStatus.OK)
    public Map<String,Object> priceAction(@PathVariable String code, @PathVariable String periods) {
        log.info("price action for {} over periods {}",code,periods);
        Map<Integer, List<RingBuffer<PriceAction>>> buffers = new HashMap<>();
        List<CalculatingRingBuffer<PriceAction, PriceAction, PriceAction>> x = Stream.of(periods.split(","))
                .map(String::trim)
                .flatMap(c -> PriceActionFunctions.match(buffers, c).stream())
                .collect(Collectors.toList());
        x.stream().map(RingBuffer::capacity).max(Comparator.naturalOrder())
                .ifPresent(maxPeriod -> repository.findByCode(code,maxPeriod).stream().forEach(p -> {
                    x.stream().forEach(b -> b.write(p));
                }));
        return x.stream().collect(Collectors.toMap(CalculatingRingBuffer::name, CalculatingRingBuffer::calculate));
    }
}
