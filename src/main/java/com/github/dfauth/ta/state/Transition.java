package com.github.dfauth.ta.state;

import java.util.Optional;
import java.util.function.Predicate;

import static java.util.function.Predicate.not;

public interface Transition<T,U,E> extends Keyed<E,Transition<T,U,E>> {

    static <T,U,E> Builder<T,U,E> builder(State.Builder<T,U,E> parent) {
        return new Builder<>(parent);
    }

    State<T,U,E> next(E event);

    E event();

    default E getKey() {
        return event();
    }

    default Optional<State<T,U,E>> apply(U ctx, E event) {
        return Optional.of(event).filter(event()::equals).filter(guard(ctx)).map(this::next);
    }

    Predicate<E> guard(U ctx);

    class Builder<T,U,E> {
        private final State.Builder<T,U,E> parent;
        private T payload;
        private E event;
        private Predicate<E> guard;
        private T next;

        public Builder(State.Builder<T, U, E> parent) {
            this.parent = parent;
        }

        public Transition<T, U, E> build() {
            return new Transition<>() {
                @Override
                public State<T, U, E> next(E event) {
                    return parent.getStateMap().get(next);
                }

                @Override
                public E event() {
                    return event;
                }

                @Override
                public Predicate<E> guard(U ctx) {
                    return guard;
                }
            };
        }

        public Builder<T,U,E> inState(T t) {
            this.payload = t;
            return this;
        }

        public Builder<T,U,E> onEvent(E e) {
            this.event = e;
            return this;
        }

        public Builder<T, U, E> unless(Predicate<E> p) {
            this.guard = not(p);
            return this;
        }

        public Builder<T, U, E> transitionTo(T t) {
            this.next = t;
            return this;
        }
    }
}
