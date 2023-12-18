package com.github.dfauth.ta.state;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class StateMachine<T,U,E> {

    private final AtomicReference<State<T,U,E>> currentState;
    private final Executor executor;

    public StateMachine(State<T, U, E> initialState, Executor executor) {
        this.currentState = new AtomicReference<>(initialState);
        this.executor = executor;
    }

    public static <T,U,E> StateMachine.Builder<T,U,E> builder() {
        return new StateMachine.Builder<T,U,E>();
    }


    public void onEvent(U ctx, E event) {
        currentState.getAndUpdate(current -> current.onEvent(ctx, event).map(s -> {
            executor.execute(() -> {
                current.onExit(ctx,s,event);
                s.onEntry(ctx,current,event);
            });;
            return s;
        }).orElse(current));
    }

    public State<T,U,E> getCurrentState() {
        return currentState.get();
    }

    public static class Builder<T,U,E> {

        private State<T,U,E> initial;
        private Map<T,State<T,U,E>> stateMap = new HashMap<>();
        private Executor executor = ForkJoinPool.commonPool();

        public Builder<T, U, E> initial(Consumer<State.Builder<T,U,E>> consumer) {
            State.Builder<T, U, E> initialBuilder = State.builder(this);
            consumer.accept(initialBuilder);
            initial = initialBuilder.build();
            stateMap.put(initial.getKey(), initial);
            return this;
        }

        public Builder<T, U, E> withState(Consumer<State.Builder<T,U,E>> consumer) {
            State.Builder<T, U, E> builder = State.builder(this);
            consumer.accept(builder);
            State<T, U, E> state = builder.build();
            stateMap.put(state.getPayload(), state);
            return this;
        }

        public StateMachine<T,U,E> build() {
            return new StateMachine<>(stateMap.get(initial.getPayload()), executor);
        }

        public Map<T, State<T, U, E>> getStateMap() {
            return stateMap;
        }

        public Builder<T, U, E> withExecutor(Executor executor) {
            this.executor = executor;
            return this;
        }
    }
}
