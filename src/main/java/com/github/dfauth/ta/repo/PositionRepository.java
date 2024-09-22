package com.github.dfauth.ta.repo;

import com.github.dfauth.ta.model.Position;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface PositionRepository extends CrudRepository<Position, Integer> {

    @Query(value = "SELECT * FROM POSITION WHERE SIZE > 0", nativeQuery = true)
    List<Position> findNonZeroPositions();

    default List<Position> findPositions(int period) {
        return List.of();
    }

    @Query(value = "SELECT * FROM POSITION WHERE CODE = ?1", nativeQuery = true)
    List<Position> findByCode(String code);

    default Optional<Position> findPositionByCode(String code) {
        return findByCode(code).stream().findFirst();
    }
}
