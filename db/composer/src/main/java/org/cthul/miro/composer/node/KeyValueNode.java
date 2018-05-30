package org.cthul.miro.composer.node;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
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
    public default void batch(Collection<? extends Object> values) {
        if (values.size() == 2) {
            Iterator<?> it = values.iterator();
            put((K) it.next(), (V) it.next());
        } else if (values.size() > 0) {
            put(map(values.toArray()));
        }
    }

    @Override
    public default void batch(Object... values) {
        if (values.length == 2) {
            put((K) values[0], (V) values[1]);
        } else if (values.length > 0) {
            put(map(values));
        }
    }
    
    static <K, V> Map<K, V> map(Object... values) {
        if (values.length % 2 != 0) throw new IllegalArgumentException("Expected key-value pairs");
        Map<K, V> map = new HashMap<>(3 * values.length / 4);
        for (int i = 0; i < values.length; i += 2) {
            K k = (K) values[i];
            V v = (V) values[i+1];
            map.put(k, v);
        }
        return map;
    }
}
