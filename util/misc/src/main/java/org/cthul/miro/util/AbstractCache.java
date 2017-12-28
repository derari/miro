package org.cthul.miro.util;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A map that automatically creates values for missing keys.
 * @param <K>
 * @param <V>
 */
public abstract class AbstractCache<K, V> {

    private final ConcurrentHashMap<K, V> cache = new ConcurrentHashMap<>();

    /**
     * Returns the current value for key or null if it is missing.
     * @param key
     * @return value if exists
     */
    protected V peekValue(K key) {
        V value = cache.get(key);
        if (value == NULL) return null;
        return value;
    }
    
    /**
     * Returns the value for key, creating it if necessary.
     * @param key
     * @return value
     */
    protected V getValue(K key) {
        V value = cache.get(key);
        if (value != null) {
            if (value == NULL) return null;
            return value;
        }
        return putNew(key);
    }
    
    /**
     * Overwrites the value for key.
     * @param key
     * @param value
     * @return old value
     */
    protected V forcePut(K key, V value) {
        return cache.put(key, value);
    }
    
    /**
     * Stores the value for key if it is absent, returns the current value.
     * @param key
     * @param value
     * @return current value
     */
    protected V tryPut(K key, V value) {
        V v = value == null ? (V) NULL : value;
        V old = cache.putIfAbsent(key, v);
        return old == null ? value :
               old == NULL ? null : old;
    }
    
    /**
     * Computes and stores the value for key.
     * @param key
     * @return current value
     */
    protected V putNew(K key) {
        V value;
        try {
            value = create(key);
        } catch (Exception e) {
            throw new IllegalArgumentException(String.valueOf(key), e);
        }
        return tryPut(key, value);
    }
    
    protected boolean isEmptyCache() {
        return cache.isEmpty();
    }
    
    protected Set<Map.Entry<K, V>> entrySet() {
        return cache.entrySet();
    }
    
    protected int size() {
        return cache.size();
    }
    
    /**
     * Creates the value for key.
     * @param key
     * @return new value
     */
    protected abstract V create(K key);
    
    private final Object NULL = new Object();
}
