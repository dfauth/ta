package com.github.dfauth.ta.repo;

import com.github.dfauth.ta.model.CodeDateCompositeKey;
import com.github.dfauth.ta.model.Position;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

public interface PositionRepository extends CrudRepository<Position, CodeDateCompositeKey> {

    @Query(value = "SELECT * FROM POSITION WHERE SIZE > 0", nativeQuery = true)
    List<Position> findNonZeroPositions();

    @Query(value = "SELECT * FROM POSITION WHERE CODE = ?1 ORDER BY DATE DESC", nativeQuery = true)
    List<Position> findByCode(String code);

    default Optional<Position> findPositionByCode(String code) {
        return findByCode(code).stream().findFirst();
    }

    @Query(value = "SELECT * FROM POSITION WHERE CODE = ?1 AND DATE <= ?2 ORDER BY DATE DESC", nativeQuery = true)
    List<Position> _findPositionByCodeAndDate(String code, Timestamp date);

    default Optional<Position> findPositionByCodeAndDate(String code, Timestamp date) {
        return _findPositionByCodeAndDate(code, date).stream().findFirst();
    }

    @Query(value = "SELECT * FROM POSITION WHERE SIZE != 0 ORDER BY DATE DESC", nativeQuery = true)
    List<Position> findAllNonZeroPositions();

    @Query(value = "SELECT p from Position p where p.date < ?1")
    Iterable<Position> findAllPriorTo(Timestamp timestamp);

    default Iterable<Position> findStartingOnOrAfter(LocalDate date) {
        return findBetween(new Timestamp(date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()), new Timestamp(System.currentTimeMillis()));
    }

    default Iterable<Position> findBetween(LocalDate from, LocalDate to) {
        return findBetween(new Timestamp(from.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()),
                new Timestamp(to.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()));
    }

    @Query(value = "SELECT p from Position p where p.date >= ?1 and date < ?2 and p.last >= ?1 and p.last < ?2")
    Iterable<Position> findBetween(Timestamp from, Timestamp to);
}
