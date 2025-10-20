package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.model.Position;
import com.github.dfauth.ta.model.PositionCollectors;
import com.github.dfauth.ta.model.Theme;
import com.github.dfauth.ta.service.NewPositionService;
import com.github.dfauth.ta.service.TradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;

import static com.github.dfauth.ta.controller.MetricsController.Mode.ALL;
import static com.github.dfauth.ta.functional.Predicates.alwaysTrue;
import static com.github.dfauth.ta.model.PositionDatePredicate.*;
import static com.github.dfauth.ta.util.StreamOps.stream;
import static java.util.Optional.empty;

@RestController
@Slf4j
@RequestMapping("/new/position")
@RequiredArgsConstructor
public class NewPositionController {

    private final NewPositionService newPositionService;
    private final TradeService tradeService;


    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public Object positions(@RequestParam("asAt") Optional<String> asAtOpt,
                            @RequestParam("startBefore") Optional<String> startBefore,
                            @RequestParam("startAfter") Optional<String> startAfter,
                            @RequestParam("endBefore") Optional<String> endBefore,
                            @RequestParam("endAfter") Optional<String> endAfter,
                            @RequestParam("mode") Optional<MetricsController.Mode> mode,
                            @RequestParam("theme") Optional<String> theme,
                            @RequestParam("excludeCodes") Optional<String> excludeCodes,
                            @RequestParam("includeCodes") Optional<String> includeCodes,
                            @RequestParam("collector") Optional<PositionCollectors> collector) {
        Predicate<Position> pred = toPredicate(startBefore,
                startAfter,
                endBefore,
                endAfter,
                mode,
                theme,
                excludeCodes,
                includeCodes);
        return newPositionService.getPositions(asAtOpt,
                pred,
                collector, stream(tradeService.findAll()));
    }

    @GetMapping("/{code}")
    @ResponseStatus(HttpStatus.OK)
    public Object position(@PathVariable("code") String code,
                           @RequestParam("asAt") Optional<String> asAtOpt,
                           @RequestParam("startBefore") Optional<String> startBefore,
                           @RequestParam("startAfter") Optional<String> startAfter,
                           @RequestParam("endBefore") Optional<String> endBefore,
                           @RequestParam("endAfter") Optional<String> endAfter,
                           @RequestParam("mode") Optional<MetricsController.Mode> mode,
                           @RequestParam("theme") Optional<String> theme,
                           @RequestParam("collector") Optional<PositionCollectors> collector) {
        Predicate<Position> pred = toPredicate(startBefore,
                startAfter,
                endBefore,
                endAfter,
                mode,
                theme,
                empty(),
                Optional.of(code.split(":")[1]));
        return newPositionService.getPositions(asAtOpt,
                pred,
                collector, stream(tradeService.findByCode(code)));
    }

    public static Predicate<Position> toPredicate(Optional<String> startBefore,
                                                  Optional<String> startAfter,
                                                  Optional<String> endBefore,
                                                  Optional<String> endAfter,
                                                  Optional<MetricsController.Mode> mode,
                                                  Optional<String> theme,
                                                  Optional<String> excludeCodes,
                                                  Optional<String> includeCodes) {
        Predicate<Position> startDatePredicate = startBefore.map(START_BEFORE).orElse(ignore()).and(startAfter.map(START_AFTER).orElse(ignore()));
        Predicate<Position> endDatePredicate = endBefore.map(END_BEFORE).orElse(ignore()).and(endAfter.map(END_AFTER).orElse(ignore()));
        Predicate<Position> modePredicate = mode.orElse(ALL);
        Predicate<Position> themePredicate = theme.map(Theme::fromString).orElse(alwaysTrue());
        Predicate<Position> excludeCodesPredicate = excludeCodes.map(str -> (Predicate<Position>)(p -> !Arrays.stream(str.split(",")).map(c -> "ASX:"+c.trim()).toList().contains(p.getCode()))).orElse(alwaysTrue());
        Predicate<Position> includeCodesPredicate = includeCodes.map(str -> (Predicate<Position>)(p -> Arrays.stream(str.split(",")).map(c -> "ASX:"+c.trim()).toList().contains(p.getCode()))).orElse(alwaysTrue());
        return startDatePredicate
                .and(endDatePredicate)
                .and(modePredicate)
                .and(themePredicate)
                .and(excludeCodesPredicate)
                .and(includeCodesPredicate);
    }

}
