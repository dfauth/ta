package com.github.dfauth.ta.repo;

import com.github.dfauth.ta.model.CodeDateCompositeKey;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.github.dfauth.ta.util.DateTimeUtils.toTimestamp;

public interface PositionRepository extends CrudRepository<Position, CodeDateCompositeKey> {

    @Query(value = "SELECT * FROM POSITION WHERE SIZE > 0", nativeQuery = true)
    List<Position> findNonZeroPositions();

    @Query(value = "SELECT * FROM POSITION WHERE CODE = ?1 ORDER BY DATE DESC", nativeQuery = true)
    List<Position> findByCode(String code);

    default Optional<Position> findPositionByCode(String code) {
        return findByCode(code).stream().findFirst();
    }

    default Optional<Position> findPositionByCodeAndDate(String code, LocalDate date) {
        return findPositionByCodeAndDate(code, toTimestamp(date));
    }

    @Query(value = "SELECT * FROM POSITION WHERE CODE = ?1 AND DATE <= ?2", nativeQuery = true)
    Optional<Position> findPositionByCodeAndDate(String code, Timestamp date);

    @Query(value = "SELECT * FROM POSITION WHERE SIZE != 0 ORDER BY DATE DESC", nativeQuery = true)
    List<Position> findAllNonZeroPositions();


    default Iterable<Position> findAllPriorTo(LocalDate date) {
        return findAllPriorTo(toTimestamp(date));
    }

    @Query(value = "SELECT p from Position p where p.date < ?1")
    Iterable<Position> findAllPriorTo(Timestamp timestamp);

    default Iterable<Position> findStartingOnOrAfter(LocalDate date) {
        return findBetween(toTimestamp(date), toTimestamp(LocalDate.now()));
    }

    default Iterable<Position> findBetween(LocalDate from, LocalDate to) {
        return findBetween(toTimestamp(from),
                toTimestamp(to));
    }

    @Query(value = "SELECT p from Position p where p.date >= ?1 and date < ?2 and p.last >= ?1 and p.last < ?2")
    Iterable<Position> findBetween(Timestamp from, Timestamp to);
}
