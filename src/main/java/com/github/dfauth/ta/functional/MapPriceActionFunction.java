package com.github.dfauth.ta.functional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public abstract class MapPriceActionFunction<A> extends PriceActionFunction<A, Map<String,Object>> {

    @Override
    public Function<AtomicReference<A>, Map<String, Object>> finisher() {
        return ref -> mapObject(ref.get());
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T> Map<String,Object> mapObject(T t) {
        return objectMapper.convertValue(t, new TypeReference<Map<String, Object>>() {});
    }
}
