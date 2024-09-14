package com.github.dfauth.ta.repo;

import com.github.dfauth.ta.model.RankListDateCodeComposite;
import com.github.dfauth.ta.model.ListDateCodeCompositeKey;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface RankingRepository extends CrudRepository<RankListDateCodeComposite, ListDateCodeCompositeKey> {

    @Query(value = "SELECT * FROM RANKING WHERE ID = ?1", nativeQuery = true)
    List<RankListDateCodeComposite> findByRanking(int id);

    @Query(value = "SELECT * FROM RANKING WHERE ID = ?1 AND CODE= ?2", nativeQuery = true)
    List<RankListDateCodeComposite> findByCode(int id, String code);

    @Query(value = "SELECT MAX(DATE) FROM RANKING WHERE ID = ?1", nativeQuery = true)
    Timestamp findMaxDate(int id);

    default Optional<RankListDateCodeComposite> findCurrentByCode(int id, String code) {
        return findByCodeAndDate(id, Optional.of(code.split(":")).filter(array -> array.length> 1).map(array -> array[1]).orElse(code), findMaxDate(id));
    }

    @Query(value = "SELECT * FROM RANKING r WHERE r.id = ?1 and r.code = ?2 and r.DATE = ?3", nativeQuery = true)
    Optional<RankListDateCodeComposite> findByCodeAndDate(int id, String code, Timestamp maxDate);

}
