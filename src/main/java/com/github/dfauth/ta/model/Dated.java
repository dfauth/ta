package com.github.dfauth.ta.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface Dated<T> {
    LocalDate getLocalDate();

    @JsonIgnore
    T getPayload();

    default <R> Dated<R> map(Function<T,R> f) {
        return dated(getLocalDate(), f.apply(getPayload()));
    }

    default <R> Dated<R> flatMap(Function<T,Dated<R>> f) {
        return f.apply(getPayload());
    }

    default <R,U> Function<Dated<R>, Optional<Dated<U>>> map(BiFunction<T,R,U> f) {
        return r -> apply(f).apply(this,r);
    }

    static <T,R,U> BiFunction<Dated<T>, Dated<R>, Optional<Dated<U>>> apply(BiFunction<T,R,U> f) {
        return (t,r) -> Optional.of(r).filter(_r -> _r.getLocalDate().equals(t.getLocalDate())).map(_r -> dated(_r.getLocalDate(), f.apply(t.getPayload(), _r.getPayload())));
    }

    static <T,R> Function<Dated<T>, Dated<R>> apply(Function<T,R> f) {
        return t -> t.map(f);
    }

    static <R> Dated<R> dated(LocalDate timestamp, R r) {
        return new Dated<>(){
            @Override
            public LocalDate getLocalDate() {
                return timestamp;
            }

            @Override
            public R getPayload() {
                return r;
            }
        };
    }
}
