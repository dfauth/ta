package com.github.dfauth.ta.util;

import java.util.function.Supplier;

public class TryCatch {

    public static <T> T tryFinally(Supplier<T> supplier, Runnable runnable) {
        try {
            return supplier.get();
        } finally {
            runnable.run();
        }
    }

}
