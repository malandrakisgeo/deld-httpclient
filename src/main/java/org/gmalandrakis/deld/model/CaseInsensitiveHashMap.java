package org.gmalandrakis.deld.model;

import java.util.HashMap;

public class CaseInsensitiveHashMap<K extends String, V> extends HashMap<K, V> {

    @Override
    public V put(K key, V value) {
        var k = key.toLowerCase();
        return super.put((K) k, value);
    }

    @Override
    public V get(Object key) {
        return this.get((String) key);
    }

    public V get(String key) {
        return super.get(key.toLowerCase());
    }


}
