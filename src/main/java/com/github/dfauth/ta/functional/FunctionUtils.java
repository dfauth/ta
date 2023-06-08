package com.github.dfauth.ta.functional;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static java.util.function.Predicate.not;

public class FunctionUtils {

    public static <T> Function<BigDecimal, Optional<T>> nullZero(Function<BigDecimal, T> f) {
        return bd -> Optional.ofNullable(bd).filter(not(_bd -> _bd.signum() == 0)).map(f);
    }

    public static <T> Function<T, Optional<T>> windowfy(int size, Function<List<T>,Optional<T>> f) {
        List<T> l = new LinkedList<>();
        return t -> {
            l.add(t);
            if(l.size() > size) {
                l.remove(0);
            }
            return f.apply(l);
        };
    }

}
