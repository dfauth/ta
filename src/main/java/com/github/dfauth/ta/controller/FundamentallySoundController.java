package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.model.CodeDateComposite;
import com.github.dfauth.ta.repo.FundamentallySoundRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.function.Predicate.not;

@RestController
@Slf4j
public class FundamentallySoundController {

    @Autowired
    private FundamentallySoundRepository repository;

    @GetMapping("/sound/{code}")
    @ResponseStatus(HttpStatus.OK)
    public Boolean fundamentallySound(@PathVariable String code) {
        log.info("is fundamentally sound {}",code);
        return repository.findCurrentByCode(code).isPresent();
    }

    @PostMapping("/sync/sound/{_date}")
    @ResponseStatus(HttpStatus.OK)
    public Long fundamentallySound(@PathVariable Integer _date, @RequestBody List<List<String>> o) {
        log.info("fundamentallySound {} {}",_date,o);
        LocalDateTime dt = LocalDateTime.of(LocalDate.of(_date / 100, _date % 100, 1), LocalTime.of(0,0));
        Timestamp timestamp = new Timestamp(dt.toInstant(ZoneOffset.UTC).toEpochMilli());
        List<CodeDateComposite> codes = o.stream().flatMap(Collection::stream).filter(not(""::equals)).map(c -> new CodeDateComposite(timestamp, c)).collect(Collectors.toList());
        Iterable<CodeDateComposite> it = repository.saveAll(codes);
        return StreamSupport.stream(it.spliterator(), false).count();
    }
}
