package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.model.Indx;
import com.github.dfauth.ta.repo.IndexRepository;
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
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.function.Predicate.not;

@RestController
@Slf4j
public class IndexController implements ControllerMixIn {

    @Autowired
    private IndexRepository repository;

    @GetMapping("/index/{idx}/{code}")
    @ResponseStatus(HttpStatus.OK)
    public Boolean isInIndex(@PathVariable String idx,@PathVariable String code) {
        log.info("is in index {} {}",idx, code);
        return repository.findCurrentByCode(code).isPresent();
    }

    @PostMapping("/index/{idx}")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Boolean> isInIndex(@PathVariable String idx, @RequestBody List<List<String>> codes) {
        log.info("is in index {} {}",idx, codes);
        return mapCode(codes, code -> isInIndex(idx,code));
    }

    @PostMapping("/sync/index/{idx}/{_date}")
    @ResponseStatus(HttpStatus.OK)
    public Long syncIndex(@PathVariable String idx,@PathVariable Integer _date, @RequestBody List<List<String>> o) {
        log.info("syncIndex {} {} {}",idx,_date,o);
        LocalDateTime dt = LocalDateTime.of(LocalDate.of(_date / 100, _date % 100, 1), LocalTime.of(0,0));
        Timestamp timestamp = new Timestamp(dt.toInstant(ZoneOffset.UTC).toEpochMilli());
        List<Indx> codes = o.stream().flatMap(Collection::stream).filter(not(""::equals)).map(c -> new Indx(idx,timestamp, c)).collect(Collectors.toList());
        Iterable<Indx> it = repository.saveAll(codes);
        return StreamSupport.stream(it.spliterator(), false).count();
    }
}
