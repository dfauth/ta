package com.github.dfauth.ta.state;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

@Data
@EqualsAndHashCode
@AllArgsConstructor
public class State<T,U,E> {

    private final T payload;
    private List<Transition<T,U,E>> transitions;

    public Optional<State<T, U, E>> onEvent(U ctx, E event) {
        return transitions.stream().map(t -> t.apply(ctx,event)).flatMap(Optional::stream).findFirst();
    }

    public void onExit(U ctx) {

    }

    public void onEntry(U ctx) {

    }

    public <R> R callback(BiFunction<T,U,R> f2, U ctx) {
        return f2.apply(payload, ctx);
    }
}
