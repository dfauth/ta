package com.github.dfauth.ta.repo;

import com.github.dfauth.ta.model.CodeDateCompositeKey;
import com.github.dfauth.ta.model.MktDepth;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

public interface MktDepthRepository extends CrudRepository<MktDepth, CodeDateCompositeKey> {

    @Query(value = "SELECT md FROM MktDepth md where md.code = ?1 order by md.date desc")
    List<MktDepth> findById(String code);

    default List<MktDepth> findLatest(LocalDate date) {
        return findLatest(new Timestamp(date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()));
    }

    @Query(value = "SELECT md FROM MktDepth md where md.date >= ?1")
    List<MktDepth> findLatest(Timestamp date);

    @Query(value = "SELECT md FROM MktDepth md where md.code = ?1 and md.date >= ?2")
    List<MktDepth> findByIdAndDateAfter(String code, Timestamp date);

    default Optional<MktDepth> findByIdAndDate(String code, Timestamp date) {
        return findByIdAndDateAfter(code, date).stream().findFirst();
    }

    @Query(value = "SELECT md FROM MktDepth md where md.date >= ?1")
    List<MktDepth> findByDate(Timestamp date);
}
