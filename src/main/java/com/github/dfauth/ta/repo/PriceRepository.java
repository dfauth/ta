package com.github.dfauth.ta.repo;

import com.github.dfauth.ta.functional.Collectors;
import com.github.dfauth.ta.functional.Tuple2;
import com.github.dfauth.ta.model.Market;
import com.github.dfauth.ta.model.Price;
import com.github.dfauth.ta.model.PriceCompositeKey;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static com.github.dfauth.ta.util.DateTimeUtils.toTimestamp;

public interface PriceRepository extends CrudRepository<Price, PriceCompositeKey> {

    default List<Price> findByCode(String _code, int limit) {
        List<Price> l = findLatestBy_code(_code, limit);
        Collections.reverse(l);
        return l;
    }

    default Optional<Price> findByCodeAndDate(String code, LocalDate date) {
        return findByCodeAndDate(code, toTimestamp(date)).stream().findFirst();
    }

    @Query(value = "SELECT * FROM Price p WHERE p.code = ?1 order by date desc", nativeQuery = true)
    List<Price> findByCodeOrderByDate(String code);

    @Query(value = "SELECT * FROM Price p WHERE p.code = ?1 and p.date <= ?2 order by p.date desc LIMIT 1", nativeQuery = true)
    List<Price> findByCodeAndDate(String code, Timestamp date);

    default List<Price> findByCodeAndDate(String _code, Timestamp marketDate, int limit) {
        List<Price> l = findLatestByDate(_code, marketDate, limit);
        Collections.reverse(l);
        return l;
    }

    default Optional<Price> findLatestByCode(String _code) {
        return findLatestBy_code(_code, 1).stream().findFirst();
    }

    @Query(value = "SELECT * FROM Price p WHERE p.code = ?1 order by p.date DESC LIMIT ?2", nativeQuery = true)
    List<Price> findLatestBy_code(String code, int limit);

    @Query(value = "SELECT CODE FROM Price p WHERE p.date < ?1 order by CODE DESC", nativeQuery = true)
    List<String> findCodesByDate(Date date);

    @Query(value = "SELECT * FROM Price p WHERE p.code = ?1 and p.date < ?2 order by p.date desc LIMIT ?3", nativeQuery = true)
    List<Price> findLatestByDate(String _code, Date date, int limit);

    @Query(value = "SELECT max(date) FROM Price", nativeQuery = true)
    Timestamp latestPriceDate();

    @Query(value = "SELECT * FROM Price where date >= ?1", nativeQuery = true)
    List<Price> activeAsAtDate(Timestamp date);

    default Optional<Price> findLatestByCodeAndDate(String code, LocalDate date, Market market) {
        Instant i = market.atMarketCloseOnOrPriorTo(date);
        return findLatestByDate(code, new Timestamp(i.toEpochMilli()), 1).stream().findFirst();
    }

    @Query(value = "SELECT new com.github.dfauth.ta.functional.Tuple2(p._date, count(p)) FROM Price p group by p._date order by p._date desc")
    List<Tuple2<Timestamp, Long>> _priceCountByDate();

    default Map<LocalDate, Long> priceCountByDate() {
        return priceCountByDate(LocalDate::compareTo);
    }

    default Map<LocalDate, Long> priceCountByDate(Comparator<LocalDate> comparator) {
        return _priceCountByDate().stream()
                .map(t -> t.map((k,v) -> Map.entry(k.toLocalDateTime().toLocalDate(), v)))
                .collect(Collectors.toMap(new TreeMap<>(comparator)));
    }
}
