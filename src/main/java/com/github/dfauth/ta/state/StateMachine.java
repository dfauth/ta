package com.github.dfauth.ta.state;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicReference;

public class StateMachine<T,U,E> {

    private final AtomicReference<State<T,U,E>> currentState;
    private final Executor executor;

    public StateMachine(State<T, U, E> initialState) {
        this(initialState, ForkJoinPool.commonPool());
    }

    public StateMachine(State<T, U, E> initialState, Executor executor) {
        this.currentState = new AtomicReference<>(initialState);
        this.executor = executor;
    }


    public void onEvent(U ctx, E event) {
        currentState.getAndUpdate(current -> current.onEvent(ctx, event).map(s -> {
            executor.execute(() -> {
                current.onExit(ctx);
                s.onEntry(ctx);
            });;
            return s;
        }).orElse(current));
    }

    public State<T,U,E> getCurrentState() {
        return currentState.get();
    }

}
