package com.github.dfauth.ta.state;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public interface Transition<T,U,E>  extends BiFunction<U,E, Optional<State<T,U,E>>> {

    State<T,U,E> next(E event);

    default Optional<State<T,U,E>> apply(U ctx, E event) {
        return Optional.of(event).filter(guard(ctx)).map(this::next);
    }

    Predicate<E> guard(U ctx);

    static <T,U,E> Transition<T,U,E> transition(T target, BiPredicate<U,E> p) {

        return new Transition<>() {
            @Override
            public State<T, U, E> next(E event) {
                return new State<>(target);
            }

            @Override
            public Predicate<E> guard(U ctx) {
                return e -> p.test(ctx,e);
            }
        };
    }
}
