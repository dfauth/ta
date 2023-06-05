package com.github.dfauth.ta;

import java.util.function.Function;

public class CodeWith<T> {

    private final String code;
    private final T payload;

    public CodeWith(String code, T payload) {
        this.code = code;
        this.payload = payload;
    }

    public String code() {
        return code;
    }

    public T payload() {
        return payload;
    }

    public <R> CodeWith<R> map(Function<T,R> f) {
        return new CodeWith<>(code, f.apply(payload));
    }

    public static <T> CodeWith<T> codeWith(String code, T t) {
        return new CodeWith<>(code, t);
    }

}
