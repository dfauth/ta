package com.github.dfauth.ta.repo;

import com.github.dfauth.ta.model.Price;
import com.github.dfauth.ta.model.PriceCompositeKey;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface PriceRepository extends CrudRepository<Price, PriceCompositeKey> {

    default List<Price> findByCode(String _code, int limit) {
        List<Price> l = findLatestBy_code(_code, limit);
        Collections.reverse(l);
        return l;
    }

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

    @Query(value = "SELECT * FROM Price p WHERE p.code = ?1 and p.date < ?2 LIMIT ?3", nativeQuery = true)
    List<Price> findLatestByDate(String _code, Date date, int limit);

    @Query(value = "SELECT max(date) FROM Price", nativeQuery = true)
    Timestamp latestPriceDate();

    @Query(value = "SELECT * FROM Price where date >= ?1", nativeQuery = true)
    List<Price> activeAsAtDate(Timestamp date);

}
