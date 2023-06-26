package com.github.dfauth.ta.functional;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

public interface WindowFunction<T, R> extends Function<Collection<T>, Optional<R>> {
}
