package com.github.dfauth.ta.repo;

import com.github.dfauth.ta.model.Valuation;
import com.github.dfauth.ta.model.CodeDateCompositeKey;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public interface ValuationRepository extends CrudRepository<Valuation, CodeDateCompositeKey> {

    List<Valuation> findByCode(String code);

    default List<Valuation> findByCode(String code, int limit) {
        List<Valuation> l = findLatestByCode(code, limit);
        Collections.reverse(l);
        return l;
    }

    default Optional<Valuation> findLatestByCode(String code) {
        return findLatestByCode(code, 1).stream().findFirst();
    }

    @Query(value = "SELECT * FROM Valuation p WHERE p.code = ?1 order by p._DATE DESC LIMIT ?2", nativeQuery = true)
    List<Valuation> findLatestByCode(String _code, int limit);

    @Query(value = "SELECT v.code,v.target,p._close FROM Valuation v,Price p WHERE p._Code = v.code and v._date = ?1 and p._DATE =?2 ORDER BY (v.target - p._close)/p._close DESC LIMIT ?3", nativeQuery = true)
    List<Object[]> topPotential(Timestamp valDate, Timestamp priceDate, int limit);

    @Query(value = "SELECT max(_Date) FROM Valuation", nativeQuery = true)
    Timestamp latestValuationDate();

}
