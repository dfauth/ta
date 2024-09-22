package com.github.dfauth.ta.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class TypeToken<T> {

    public static <T> Type getTypeToken(Class<T> classOfT) {
        if(classOfT.isAssignableFrom(ParameterizedType.class)) {
            return ((ParameterizedType) classOfT.getGenericSuperclass()).getActualTypeArguments()[0];
        }
        throw new IllegalArgumentException("Not an instance of ParameterizedType");
    }

    private Type type;

    protected TypeToken(){
        this.type = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public Type getType() {
        return type;
    }
}