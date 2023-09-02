package com.github.dfauth.ta.functional;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface StatefulFunction<T,R,S> extends BiFunction<T,S,Tuple2<Optional<R>,S>> {

    static <T,R,S> Function<T,Optional<R>> asFunction(BiFunction<T,S,Optional<R>> f2) {
        return asFunction(toStatefulFunction(f2));
    }

    static <T,R,S> Function<T,Optional<R>> asFunction(StatefulFunction<T,R,S> f) {
        AtomicReference<S> state = new AtomicReference<>();
        return (t) -> {
            Tuple2<Optional<R>, S> t2 = f.apply(t, state.get());
            state.set(t2._2());
            return t2._1();
        };
    }

    static <T,R,S> StatefulFunction<T,R,S> toStatefulFunction(BiFunction<T,S,Optional<R>> f2) {
        return (t,s) -> new Tuple2<>(f2.apply(t,s),s);
    }

}
