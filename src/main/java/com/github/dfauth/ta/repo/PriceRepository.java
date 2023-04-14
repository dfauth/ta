package com.github.dfauth.ta.repo;

import com.github.dfauth.ta.model.Price;
import com.github.dfauth.ta.model.PriceCompositeKey;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public interface PriceRepository extends CrudRepository<Price, PriceCompositeKey> {

    List<Price> findBy_code(String _code);

    default List<Price> findByCode(String _code) {
        return findBy_code(_code);
    }

    default List<Price> findByCode(String _code, int limit) {
        List<Price> l = findLatestBy_code(_code, limit);
        Collections.reverse(l);
        return l;
    }

    default Optional<Price> findLatestByCode(String _code) {
        return findLatestBy_code(_code, 1).stream().findFirst();
    }

    @Query(value = "SELECT * FROM Price p WHERE p._Code = ?1 order by p._DATE DESC LIMIT ?2", nativeQuery = true)
    List<Price> findLatestBy_code(String _code, int limit);

}
