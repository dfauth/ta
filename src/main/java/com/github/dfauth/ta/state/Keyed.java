package com.github.dfauth.ta.state;

import java.util.Map;

public interface Keyed<K,V extends Keyed<K,V>> {

    K getKey();

    default Map.Entry<K, Keyed<K, V>> toMapEntry() {
        return Map.entry(getKey(), this);
    }

}
