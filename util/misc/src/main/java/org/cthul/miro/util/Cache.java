package org.cthul.miro.util;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * A cache that creates missing values with a function.
 */
public class Cache<K, V> extends AbstractCache<K, V> {
    
    private final Function<K, V> factory;

    public Cache(Function<K, V> factory) {
        this.factory = factory;
    }

    @Override
    protected V create(K key) {
        return factory.apply(key);
    }
    
    /**
     * Returns this cache as a {@link Map}.
     * @return map 
     */
    public Map<K, V> asMap() {
        return new AbstractMap<K, V>() {
            @Override
            public V get(Object key) {
                return Cache.this.getValue((K) key);
            }
            @Override
            public V put(K key, V value) {
                return Cache.this.tryPut(key, value);
            }
            @Override
            public Set<Map.Entry<K, V>> entrySet() {
                return Cache.this.entrySet();
            }
        };
    }
    
    public static <K, V> Map<K, V> map(Function<K, V> factory) {
        return new Cache<>(factory).asMap();
    }
}
