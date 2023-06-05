package com.github.dfauth.ta.service;

import com.github.dfauth.ta.functional.Tuple2;
import com.github.dfauth.ta.model.Price;
import com.github.dfauth.ta.repo.PriceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.dfauth.ta.functional.Lists.mapList;
import static com.github.dfauth.ta.functional.Tuple2.tuple2;

@Slf4j
@Component
public class PriceService {

    @Autowired
    private PriceRepository priceRepository;

    public List<Price> activeLatestPrice() {
        Timestamp latestPriceDate = priceRepository.latestPriceDate();
        return priceRepository.activeAsAtDate(latestPriceDate);
    }

    public Stream<List<Price>> activeLatestPricesAsStream(int windowSize) {
        return activeLatestPrice().stream().map(p -> {
            return priceRepository.findLatestBy_code(p.get_code(),windowSize);
        });
    }

    public List<Tuple2<String, BigDecimal>> momentum(int windowSize, Function<List<BigDecimal>, Optional<BigDecimal>> f) {
        return momentum(windowSize, Price::get_close, f, BigDecimal::compareTo);
    }

    public List<Tuple2<String, BigDecimal>> momentum(int windowSize, Function<Price,BigDecimal> extractor, Function<List<BigDecimal>, Optional<BigDecimal>> f, Comparator<? super BigDecimal> comparator) {
        List<Tuple2<String, BigDecimal>> result = activeLatestPricesAsStream(windowSize)
                .map(l -> tuple2(l.get(0).get_code(), mapList(l, extractor)))
                .map(t -> t.mapValue(f))
                .flatMap(t -> t.map(_t -> _t._2().map(v -> tuple2(_t._1(), v))).stream())
                .collect(Collectors.toList());
        result.sort((a,b) -> comparator.compare(a._2(), b._2()));
        return result;
    }
}
