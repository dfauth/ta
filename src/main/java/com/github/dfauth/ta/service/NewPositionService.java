package com.github.dfauth.ta.service;

import com.github.dfauth.ta.model.Position;
import com.github.dfauth.ta.model.PositionCollectors;
import com.github.dfauth.ta.model.PositionFactoryCollector;
import com.github.dfauth.ta.model.Trade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static com.github.dfauth.ta.util.DateTimeUtils.Format.YYYYMMDD;
import static java.time.LocalDate.now;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewPositionService {

    private final PriceService priceService;
    private final TransactionService transactionService;

    public Object getPositions(Optional<String> asAtOpt,
                             Predicate<Position> predicate,
                             Optional<PositionCollectors> collector,
                             Stream<Trade> tradeStream) {
        LocalDate asAt = asAtOpt.map(YYYYMMDD::toLocalDate).orElse(now());
        return getPositions(asAt, predicate, collector, tradeStream);
    }

    public Object getPositions(LocalDate asAt,
                             Predicate<Position> predicate,
                             Optional<PositionCollectors> collector,
                             Stream<Trade> tradeStream) {

        Collector<Position, Object, Object> d = collector.map(PositionCollectors::toCollector).orElse(PositionCollectors.defaultCollector());
        return getPositionFactory(tradeStream).values().stream().flatMap(List::stream)
                .map(f -> f.create(asAt))
                .flatMap(Optional::stream)
                .filter(predicate)
                .collect(d);
    }

    public Map<String, List<PositionFactoryCollector.PositionFactory>> getPositionFactory(Stream<Trade> tradeStream) {

        return tradeStream
                .collect(new PositionFactoryCollector(
                                (c,d) -> priceService.getPrice(c, d).orElse(null),
                                transactionService
                        )
                );
    }
}
