package com.github.dfauth.ta.repo;

import com.github.dfauth.ta.model.CodeDateComposite;
import com.github.dfauth.ta.model.CodeDateCompositeKey;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface FundamentallySoundRepository extends CrudRepository<CodeDateComposite, CodeDateCompositeKey> {

    List<CodeDateComposite> findByCode(String code);

    @Query(value = "SELECT MAX(_DATE) FROM FUNDAMENTALLY_SOUND", nativeQuery = true)
    Timestamp findMaxDate();

    default Optional<CodeDateComposite> findCurrentByCode(String code) {
        return findByCodeAndDate(Optional.of(code.split(":")).filter(array -> array.length> 1).map(array -> array[1]).orElse(code), findMaxDate());
    }

    @Query(value = "SELECT * FROM FUNDAMENTALLY_SOUND fs WHERE fs.code = ?1 and fs._DATE = ?2", nativeQuery = true)
    Optional<CodeDateComposite> findByCodeAndDate(String code, Timestamp maxDate);

}
