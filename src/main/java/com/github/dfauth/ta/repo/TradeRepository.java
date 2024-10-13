package com.github.dfauth.ta.repo;

import com.github.dfauth.ta.functional.Lists;
import com.github.dfauth.ta.model.Position;
import com.github.dfauth.ta.model.Trade;
import com.github.dfauth.ta.util.StreamOps;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.dfauth.ta.util.DateTimeUtils.Format.YYYYMMDD;

public interface TradeRepository extends CrudRepository<Trade, String> {

    @Query(value = "SELECT * FROM Trade t WHERE t.CONFIRMATION_NO = ?1", nativeQuery = true)
    Trade findByConfirmationNo(String confirmation_no);

    @Query(value = "SELECT * FROM Trade t WHERE t.CODE = ?1 ORDER BY DATE ASC", nativeQuery = true)
    List<Trade> findByCode(String code);

    default Trade findBy_date_code_size(Trade trade) {
        String confirmation_no = String.format("%s_%s_%d", YYYYMMDD.format(trade.getDate().toInstant()),trade.getCode(),trade.getSize());
        return findByConfirmationNo(confirmation_no);
    }


    @Query(value = "SELECT t.code, sum(t.side*t.cost) as COST,sum(t.size*t.side) as SIZE FROM Trade t GROUP by t.CODE", nativeQuery = true)
    List<Map<String,Object>> aggregateTrades();

    default List<Position> derivePositions() {
        Map<String, List<Position>> map = new HashMap<>();
        StreamOps.stream(findAllByDate()).forEach(t -> map.compute(
                t.getCode(),
                (k, v) -> Optional.ofNullable(v)
                        .map(l -> {
                            return Lists.add(l, Lists.last(l).map(p -> p.onTrade(t)).orElseThrow());
                        })
                        .orElseGet(() -> List.of(new Position(t)))
        ));
        return map.values().stream().flatMap(List::stream).collect(Collectors.toList());
    }

    @Query(value = "SELECT t FROM Trade t order by t.date asc")
    Iterable<Trade> findAllByDate();

    @Query(value = "SELECT t FROM Trade t, Position p where t.code = p.code and p.size > 0 order by t.date", nativeQuery = true)
    Iterable<Trade> findOpenPositionEvents();

}
