package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.functional.ForceIndex;
import com.github.dfauth.ta.model.PriceAction;
import com.github.dfauth.ta.repo.PriceRepository;
import io.github.dfauth.trycatch.Try;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.github.dfauth.ta.functional.Lists.mapList;
import static io.github.dfauth.trycatch.ExceptionalRunnable.tryCatch;
import static java.lang.Math.max;

@RestController
@Slf4j
public class ForceIndexController implements ControllerMixIn {

    @Autowired
    private PriceRepository repository;

    @GetMapping("/force/index/{code}/{smaPeriod}/{emaPeriod}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<ForceIndex> forceIndex(@PathVariable String code, @PathVariable int smaPeriod, @PathVariable int emaPeriod) {
        return tryCatch(() -> {
            log.info("force index {} {} {}",code,smaPeriod,emaPeriod);
            return ForceIndex.calculateForceIndex(mapList(repository.findByCode(code,max(smaPeriod,emaPeriod)+1), PriceAction.class::cast),smaPeriod, emaPeriod);
        }, ex -> Optional.empty());
    }

    @PostMapping("/force/index/{smaPeriod}/{emaPeriod}")
    @ResponseStatus(HttpStatus.OK)
    public List<Try<ForceIndex>> forceIndex(@RequestBody List<List<String>> codes, @PathVariable int smaPeriod, @PathVariable int emaPeriod) {
        log.info("force index {} {} {}",codes,smaPeriod,emaPeriod);
        return flatMapCode(codes, code -> forceIndex(code, smaPeriod, emaPeriod)).map(Map.Entry::getValue).toList();
    }
}
