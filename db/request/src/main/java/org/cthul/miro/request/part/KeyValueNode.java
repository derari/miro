package org.cthul.miro.request.part;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @param <K>
 * @param <V>
 */
public interface KeyValueNode<K, V> extends BatchNode<Object> {
    
    void put(K key, V value);
    
    void put(Map<? extends K, ? extends V> map);

    @Override
    public default void set(Collection<? extends Object> values) {
        set(values.toArray());
    }

    @Override
    public default void set(Object... values) {
        put(map(values));
    }
    
    static <K, V> Map<K, V> map(Object... values) {
        if (values.length % 2 != 0) throw new IllegalArgumentException("Expected key-value pairs");
        Map<K, V> map = new HashMap<>();
        for (int i = 0; i < values.length; i += 2) {
            K k = (K) values[i];
            V v = (V) values[i+1];
            map.put(k, v);
        }
        return map;
    }
}
