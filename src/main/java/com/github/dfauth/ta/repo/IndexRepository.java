package com.github.dfauth.ta.repo;

import com.github.dfauth.ta.model.Indx;
import com.github.dfauth.ta.model.IndxKey;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface IndexRepository extends CrudRepository<Indx, IndxKey> {

    List<Indx> findByCode(String code);

    @Query(value = "SELECT MAX(DATE) FROM INDX", nativeQuery = true)
    Timestamp findMaxDate();

    default Optional<Indx> findCurrentByCode(String code) {
        return findByCodeAndDate(Optional.of(code.split(":")).filter(array -> array.length> 1).map(array -> array[1]).orElse(code), findMaxDate());
    }

    @Query(value = "SELECT * FROM INDX fs WHERE fs.code = ?1 and fs.date = ?2", nativeQuery = true)
    Optional<Indx> findByCodeAndDate(String code, Timestamp maxDate);

}
