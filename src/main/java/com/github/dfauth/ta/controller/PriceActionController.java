package com.github.dfauth.ta.controller;

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
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.dfauth.ta.functional.IdentityPriceActionFunctions.match;

@RestController
@Slf4j
public class PriceActionController {

    @Autowired
    private PriceRepository repository;

    @GetMapping("/price/action/{code}/{periods}")
    @ResponseStatus(HttpStatus.OK)
    public Map<String,Object> priceAction(@PathVariable String code, @PathVariable String periods) {
        log.info("price action for {} over periods {}",code,periods);
        Map<Integer, RingBuffer<PriceAction>> buffers = new HashMap<>();
        Map<String, CalculatingRingBuffer<PriceAction, ?, ?>> x = Stream.of(periods.split(","))
                .map(String::trim)
                .flatMap(c -> match(buffers, c)//.orElseGet(() -> match(Arrays.<WithMatcher<PriceActionFunction<PriceAction,List<PriceAction>,?>>>stream(ListPriceActionFunctions.values()), buffers, c).orElseThrow())
                        .stream().map(crb -> Map.entry(c, crb)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        x.values().stream().map(RingBuffer::capacity).max(Comparator.naturalOrder())
                .ifPresent(maxPeriod -> repository.findByCode(code,maxPeriod).stream().forEach(p -> {
                    x.values().stream().forEach(b -> b.write(p));
                }));
        return x.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().calculate()));
    }
}
