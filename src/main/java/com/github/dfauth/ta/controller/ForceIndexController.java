package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.functional.ForceIndex;
import com.github.dfauth.ta.model.PriceAction;
import com.github.dfauth.ta.repo.PriceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static com.github.dfauth.ta.functional.Lists.mapList;
import static java.lang.Math.max;

@RestController
@Slf4j
public class ForceIndexController {

    @Autowired
    private PriceRepository repository;

    @GetMapping("/force/index/{code}/{smaPeriod}/{emaPeriod}")
    @ResponseStatus(HttpStatus.OK)
    public ForceIndex forceIndex(@PathVariable String code, @PathVariable int smaPeriod, @PathVariable int emaPeriod) {
        log.info("force index {} {} {}",code,smaPeriod,emaPeriod);
        return ForceIndex.calculateForceIndex(mapList(repository.findByCode(code,max(smaPeriod,emaPeriod)+1), PriceAction.class::cast),smaPeriod, emaPeriod);
    }
}
