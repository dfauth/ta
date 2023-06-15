package com.github.dfauth.ta.state;

import java.util.Map;

public interface SelfMapEntry<K,V extends SelfMapEntry<K,V>> extends Map.Entry<K,SelfMapEntry<K,V>> {

    default V getValue() {
        return (V) this;
    }

    @Override
    default SelfMapEntry<K, V> setValue(SelfMapEntry<K, V> value) {
        throw new UnsupportedOperationException();
    }
}
