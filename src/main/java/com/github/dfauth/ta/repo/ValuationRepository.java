package com.github.dfauth.ta.repo;

import com.github.dfauth.ta.model.Valuation;
import com.github.dfauth.ta.model.ValuationCompositeKey;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.sql.Timestamp;
import java.util.List;

public interface ValuationRepository extends CrudRepository<Valuation, ValuationCompositeKey> {

    @Query(value = "SELECT v.code,v.target,p._close FROM Valuation v,Price p WHERE p._Code = v.code and v._date = ?1 and p._DATE =?2 ORDER BY (v.target - p._close)/p._close DESC LIMIT ?3", nativeQuery = true)
    List<Object[]> topPotential(Timestamp valDate, Timestamp priceDate, int limit);

    @Query(value = "SELECT max(_Date) FROM Valuation", nativeQuery = true)
    Timestamp latestValuationDate();

}
