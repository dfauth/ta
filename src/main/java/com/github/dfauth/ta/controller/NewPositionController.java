package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.model.NewPosition;
import com.github.dfauth.ta.model.Position;
import com.github.dfauth.ta.model.PositionCollectors;
import com.github.dfauth.ta.service.PriceService;
import com.github.dfauth.ta.service.TradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collector;

import static com.github.dfauth.ta.functional.Predicates.alwaysTrue;
import static com.github.dfauth.ta.util.StreamOps.stream;

@RestController
@Slf4j
@RequestMapping("new/position")
@RequiredArgsConstructor
public class NewPositionController {

    private final TradeService tradeService;

    private final PriceService priceService;

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public Object positions(@RequestParam("startBefore") Optional<String> startBefore,
                                        @RequestParam("startAfter") Optional<String> startAfter,
                                        @RequestParam("endBefore") Optional<String> endBefore,
                                        @RequestParam("endAfter") Optional<String> endAfter,
                                        @RequestParam("mode") Optional<MetricsController.Mode> mode,
                                        @RequestParam("theme") Optional<String> theme,
                                        @RequestParam("excludeCodes") Optional<String> excludeCodes,
                                        @RequestParam("includeCodes") Optional<String> includeCodes,
                                        @RequestParam("collector") Optional<PositionCollectors> collector) {
//        Predicate<NewPosition> startDatePredicate = startBefore.map(START_BEFORE).orElse(ignore()).and(startAfter.map(START_AFTER).orElse(ignore()));
//        Predicate<NewPosition> endDatePredicate = endBefore.map(END_BEFORE).orElse(ignore()).and(endAfter.map(END_AFTER).orElse(ignore()));
//        Predicate<NewPosition> modePredicate = mode.orElse(ALL);
//        Predicate<NewPosition> themePredicate = theme.map(Theme::fromString).orElse(alwaysTrue());
        Predicate<NewPosition> excludeCodesPredicate = excludeCodes.map(str -> (Predicate<NewPosition>)(p -> !Arrays.stream(str.split(",")).map(c -> "ASX:"+c.trim()).toList().contains(p.getCode()))).orElse(alwaysTrue());
        Predicate<NewPosition> includeCodesPredicate = includeCodes.map(str -> (Predicate<NewPosition>)(p -> Arrays.stream(str.split(",")).map(c -> "ASX:"+c.trim()).toList().contains(p.getCode()))).orElse(alwaysTrue());

        Collector<Position, Object, Object> d = PositionCollectors.defaultCollector();
        List<NewPosition> positions = stream(tradeService.findAll())
                .collect(NewPosition.collector(priceService::getPrice));

                return positions.stream()
//                        .filter(startDatePredicate
//                        .and(endDatePredicate)
//                        .and(modePredicate)
//                        .and(themePredicate)
                        .filter(excludeCodesPredicate)
                        .filter(includeCodesPredicate)
//                .collect(collector.map(PositionCollectors::toCollector).orElse(d));
                        .toList();
    }

    @GetMapping("/{code}")
    @ResponseStatus(HttpStatus.OK)
    public Object position(@PathVariable("code") String code,
                           @RequestParam("startBefore") Optional<String> startBefore,
                                        @RequestParam("startAfter") Optional<String> startAfter,
                                        @RequestParam("endBefore") Optional<String> endBefore,
                                        @RequestParam("endAfter") Optional<String> endAfter,
                                        @RequestParam("mode") Optional<MetricsController.Mode> mode,
                                        @RequestParam("theme") Optional<String> theme,
                                        @RequestParam("collector") Optional<PositionCollectors> collector) {
//        Predicate<NewPosition> startDatePredicate = startBefore.map(START_BEFORE).orElse(ignore()).and(startAfter.map(START_AFTER).orElse(ignore()));
//        Predicate<NewPosition> endDatePredicate = endBefore.map(END_BEFORE).orElse(ignore()).and(endAfter.map(END_AFTER).orElse(ignore()));
//        Predicate<NewPosition> modePredicate = mode.orElse(ALL);
//        Predicate<NewPosition> themePredicate = theme.map(Theme::fromString).orElse(alwaysTrue());

        Collector<Position, Object, Object> d = PositionCollectors.defaultCollector();
        List<NewPosition> positions = stream(tradeService.findByCode(code))
                .collect(NewPosition.collector(priceService::getPrice));

                return positions.stream()
//                        .filter(startDatePredicate
//                        .and(endDatePredicate)
//                        .and(modePredicate)
//                        .and(themePredicate)
//                .collect(collector.map(PositionCollectors::toCollector).orElse(d));
                        .toList();
    }
}
