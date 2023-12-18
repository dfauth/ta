package com.github.dfauth.ta.state;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@AllArgsConstructor
@EqualsAndHashCode
public class State<T,U,E> implements Keyed<T,State<T,U,E>> {

    private final T payload;
    private Map<E,Transition<T,U,E>> transitions;
    private Consumer<TransitionEvent<T, U, E>> onEntryListener = null;
    private Consumer<TransitionEvent<T, U, E>> onExitListener = null;

    public static <T,U,E> Builder<T,U,E> builder(StateMachine.Builder<T,U,E> parent) {
        return new State.Builder<>(parent);
    }

    public Optional<State<T, U, E>> onEvent(U ctx, E event) {
        return Optional.ofNullable(transitions.get(event)).flatMap(t -> t.apply(ctx,event));
    }

    public void onExit(U ctx, State<T,U,E> toState, E event) {
        Optional.ofNullable(onExitListener).ifPresent(l -> l.accept(new TransitionEvent<>(this, toState, event, ctx)));
    }

    public void onEntry(U ctx, State<T,U,E> fromState, E event) {
        Optional.ofNullable(onEntryListener).ifPresent(l -> l.accept(new TransitionEvent<>(fromState, this, event, ctx)));
    }

    public <R> R callback(BiFunction<T,U,R> f2, U ctx) {
        return f2.apply(payload, ctx);
    }

    @Override
    public T getKey() {
        return payload;
    }

    @Data
    public static class Builder<T,U,E> {

        private final StateMachine.Builder<T, U, E> parent;
        private T payload;
        private Map<E,Transition<T, U, E>> transitions = new HashMap<>();
        private Consumer<TransitionEvent<T, U, E>> onEntryListener;
        private Consumer<TransitionEvent<T, U, E>> onExitListener;

        public Builder(StateMachine.Builder<T, U, E> parent) {
            this.parent = parent;
        }

        public Builder<T, U, E> withPayload(T payload) {
            this.payload = payload;
            return this;
        }

        public final Builder<T, U, E> withTransition(Consumer<Transition.Builder<T, U, E>> transition) {
            return withTransitions(Stream.of(transition).collect(Collectors.toList()));
        }

        @SafeVarargs()
        public final Builder<T, U, E> withTransitions(Consumer<Transition.Builder<T, U, E>>... transitions) {
            return withTransitions(Stream.of(transitions).collect(Collectors.toList()));
        }

        public Builder<T, U, E> withTransitions(List<Consumer<Transition.Builder<T, U, E>>> consumers) {
            consumers.forEach(c -> {
                Transition.Builder<T, U, E> builder = Transition.builder(this);
                builder.inState(this.payload);
                c.accept(builder);
                Transition<T, U, E> t = builder.build();
                transitions.put(t.getKey(),t);
            });
            return this;
        }

        public Builder<T, U, E> addTransition(Transition<T, U, E> transition) {
            this.transitions.put(transition.event(),transition);
            return this;
        }

        public State<T, U, E> build() {
            return new State<>(payload, transitions, onEntryListener, onExitListener);
        }

        public Map<T, State<T,U,E>> getStateMap() {
            return parent.getStateMap();
        }

        public Builder<T, U, E> onExit(Consumer<TransitionEvent<T,U,E>> consumer) {
            this.onExitListener = consumer;
            return this;
        }

        public Builder<T, U, E> onEntry(Consumer<TransitionEvent<T,U,E>> consumer) {
            this.onEntryListener = consumer;
            return this;
        }
    }
}
