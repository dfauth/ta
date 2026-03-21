package com.github.dfauth.ta.controller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.dfauth.trycatch.ExceptionalRunnable;
import io.github.dfauth.trycatch.Try;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static io.github.dfauth.trycatch.ExceptionalRunnable.tryCatch;
import static java.util.function.Predicate.not;

public interface ControllerMixIn {
    static <T> Function<Throwable, T> logAndReturn(T t) {
        return ex -> {
            ExceptionalRunnable.log.accept(ex);
            return t;
        };
    }

    default  <T> List<T> mapCode(List<List<String>> codes, Function<String,T> f) {
        return codes.stream()
                .flatMap(List::stream)
                .filter(not(String::isEmpty))
                .map(code -> f.apply(code))
                .toList();
    }

    default  <T> Stream<Map.Entry<String, Try<T>>> flatMapCode(List<List<String>> codes, Function<String, Optional<T>> f) {
        return codes.stream()
                .flatMap(List::stream)
                .filter(not(String::isEmpty))
                .map(c -> Map.entry(c, tryWrap(f).apply(c)));
    }

    static <R,T> Function<R, Try<T>> tryWrap(Function<R, Optional<T>> f) {
        return r -> tryCatch(() -> new SuccessWrapper<>(f.apply(r).orElseThrow(() -> new IllegalStateException("No value returned for argument "+r))), ex -> new FailureWrapper<T>(new IllegalStateException("exception for argument "+r+":"+ex.getMessage(), ex)));
    }

    public static class SuccessWrapper<T> extends Try.Success<T> {

        public SuccessWrapper(T value) {
            super(value);
        }

        @JsonIgnore
        @Override
        public boolean isSuccess() {
            return super.isSuccess();
        }

        @JsonIgnore
        @Override
        public boolean isFailure() {
            return super.isFailure();
        }
    }

    public static class FailureWrapper<T> extends Try.Failure<T> {


        public FailureWrapper(Throwable throwable) {
            super(throwable);
        }

        @JsonIgnore
        @Override
        public boolean isSuccess() {
            return super.isSuccess();
        }

        @JsonIgnore
        @Override
        public boolean isFailure() {
            return super.isFailure();
        }

        @JsonIgnore
        @Override
        public Throwable getThrowable() {
            return super.getThrowable();
        }

        @JsonIgnore
        @Override
        public T getValue() {
            return super.getValue();
        }

        public String getError() {
            return getThrowable().getMessage();
        }
    }
}
