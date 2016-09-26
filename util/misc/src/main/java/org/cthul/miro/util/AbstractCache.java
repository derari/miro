package org.cthul.miro.util;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @param <K>
 * @param <V>
 */
public abstract class AbstractCache<K, V> {

    private final ConcurrentHashMap<K, V> cache = new ConcurrentHashMap<>();

//    protected ConcurrentHashMap<K, V> getCache() {
//        return cache;
//    }
//    
    protected V peekValue(K key) {
        V value = cache.get(key);
        if (value == NULL) return null;
        return value;
    }
    
    protected V getValue(K key) {
        V value = cache.get(key);
        if (value != null) {
            if (value == NULL) return null;
            return value;
        }
        return putNew(key);
    }
    
    protected V forcePut(K key, V value) {
        return cache.put(key, value);
    }
    
    protected V tryPut(K key, V value) {
        V v = value == null ? (V) NULL : value;
        V old = cache.putIfAbsent(key, v);
        return old == null ? value : old;
    }
    
    protected V putNew(K key) {
        V value;
        try {
            value = create(key);
        } catch (Exception e) {
            throw new IllegalArgumentException(String.valueOf(key), e);
        }
        return tryPut(key, value);
    }
    
    protected boolean emptyCache() {
        return cache.isEmpty();
    }
    
    protected Set<Map.Entry<K, V>> entrySet() {
        return cache.entrySet();
    }
    
    protected int size() {
        return cache.size();
    }
    
    protected abstract V create(K key);
    
    private final Object NULL = new Object();
}
