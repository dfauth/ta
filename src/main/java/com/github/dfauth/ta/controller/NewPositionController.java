package com.github.dfauth.ta.controller;

import com.github.dfauth.ta.model.PositionCollectors;
import com.github.dfauth.ta.model.PositionFactoryCollector;
import com.github.dfauth.ta.model.Trade;
import com.github.dfauth.ta.service.PriceService;
import com.github.dfauth.ta.service.TradeService;
import com.github.dfauth.ta.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static com.github.dfauth.ta.functional.Predicates.alwaysTrue;
import static com.github.dfauth.ta.util.DateTimeUtils.Format.YYYYMMDD;
import static com.github.dfauth.ta.util.StreamOps.stream;
import static java.time.LocalDate.now;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;

@RestController
@Slf4j
@RequestMapping("new/position")
@RequiredArgsConstructor
public class NewPositionController {

    private final TradeService tradeService;

    private final PriceService priceService;

    private final TransactionService transactionService;

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
        return positions(asAtOpt,
                startBefore,
                startAfter,
                endBefore,
                endAfter,
                mode,
                theme,
                excludeCodes,
                includeCodes,
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
        return positions(asAtOpt,
                startBefore,
                startAfter,
                endBefore,
                endAfter,
                mode,
                theme,
                empty(),
                Optional.of(code.split(":")[1]),
                collector, stream(tradeService.findByCode(code)));
    }

    private Object positions(Optional<String> asAtOpt,
                             Optional<String> startBefore,
                             Optional<String> startAfter,
                             Optional<String> endBefore,
                             Optional<String> endAfter,
                             Optional<MetricsController.Mode> mode,
                             Optional<String> theme,
                             Optional<String> excludeCodes,
                             Optional<String> includeCodes,
                             Optional<PositionCollectors> collector,
                             Stream<Trade> tradeStream) {
        LocalDate asAt = asAtOpt.map(YYYYMMDD::toLocalDate).orElse(now());
//        Predicate<NewPosition> startDatePredicate = startBefore.map(START_BEFORE).orElse(ignore()).and(startAfter.map(START_AFTER).orElse(ignore()));
//        Predicate<NewPosition> endDatePredicate = endBefore.map(END_BEFORE).orElse(ignore()).and(endAfter.map(END_AFTER).orElse(ignore()));
//        Predicate<NewPosition> modePredicate = mode.orElse(ALL);
//        Predicate<NewPosition> themePredicate = theme.map(Theme::fromString).orElse(alwaysTrue());
        Predicate<PositionFactoryCollector.Position> excludeCodesPredicate = excludeCodes.map(str -> (Predicate<PositionFactoryCollector.Position>)(p -> !Arrays.stream(str.split(",")).map(c -> "ASX:"+c.trim()).toList().contains(p.getCode()))).orElse(alwaysTrue());
        Predicate<PositionFactoryCollector.Position> includeCodesPredicate = includeCodes.map(str -> (Predicate<PositionFactoryCollector.Position>)(p -> Arrays.stream(str.split(",")).map(c -> "ASX:"+c.trim()).toList().contains(p.getCode()))).orElse(alwaysTrue());

        Map<String, List<PositionFactoryCollector.PositionFactory>> positions = tradeStream
                .collect(new PositionFactoryCollector(
                            (c,d) -> priceService.getPrice(c, d).orElse(null),
                            transactionService
                        )
                );

        Collector<? super PositionFactoryCollector.Position, ? extends Object, ? extends Object> d = toList();
        return positions.values().stream().flatMap(List::stream)
                .map(f -> f.create(asAt))
                .flatMap(Optional::stream)
//                        .filter(startDatePredicate
//                        .and(endDatePredicate)
//                        .and(modePredicate)
//                        .and(themePredicate)
                .filter(excludeCodesPredicate)
                .filter(includeCodesPredicate)
                .collect(d);
    }
}
