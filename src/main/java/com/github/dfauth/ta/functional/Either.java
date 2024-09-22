package com.github.dfauth.ta.functional;

import lombok.AllArgsConstructor;

import java.util.function.Function;

public interface Either<L,R> {

    default L left() {
        throw new UnsupportedOperationException("Cannot call left() on an object of type "+this.getClass());
    }

    default Either<L,R> mapLeft(Function<L,Either<L,R>> f) {
        if(isLeft()) {
            return f.apply(left());
        } else {
            return this;
        }
    }

    default Either<L, R> mapRight(Function<R,Either<L,R>> f) {
        if(isRight()) {
            return f.apply(right());
        } else {
            return this;
        }
    }

    default R right() {
        throw new UnsupportedOperationException("Cannot call right() on an object of type "+this.getClass());
    }

    static <L,R> Either<L,R> left(L l) {
        return new Left<>(l);
    }

    static <L,R> Either<L,R> right(R r) {
        return new Right<>(r);
    }

    default boolean isRight() {
        return !isLeft();
    }

    boolean isLeft();

    @AllArgsConstructor
    class Left<L,R> implements Either<L,R> {

        private final L l;

        @Override
        public L left() {
            return l;
        }

        @Override
        public boolean isLeft() {
            return true;
        }
    }

    @AllArgsConstructor
    class Right<L,R> implements Either<L,R> {

        private final R r;

        @Override
        public R right() {
            return r;
        }

        @Override
        public boolean isLeft() {
            return false;
        }
    }
}
