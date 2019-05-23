package org.cthul.miro.composer.node;

import java.util.Map.Entry;
import java.util.*;
import org.cthul.miro.util.ArrayMap;

/**
 *
 * @param <K>
 * @param <V>
 */
public interface MultiKeyValueNode<K, V> extends BatchNode<Object> {
    
    void put(K key, V value);
    
    void put(Map<? extends K, ? extends V> map);
    
    ListNode<V[]> forKeys(K... keys);

    @Override
    public default void batch(Collection<? extends Object> values) {
        batch(values.toArray());
    }

    @Override
    public default void batch(Object... values) {
        if (values.length == 2) {
            put((K) values[0], (V) values[1]);
        } else {
            put(map(values));
//            forKeys(select2(values, 0)).addAll(select2(values, 1));
        }
    }
    
    static <K,V> Map<K,V> map(Object[] values) {
        return new ArrayMap<>(values);
    }
    
//    static <E> E[] select2(Object[] values, int offset) {
//        if (values.length % 2 != 0) throw new IllegalArgumentException("Expected key-value pairs");
//        Object[] result = offset == 0 ? new String[values.length/2] : new Object[values.length/2];
//        for (int i = 0; i < result.length; i++) {
//            result[i] = values[2*i + offset];
//        }
//        return (E[]) result;
//    }
//    
//    static <K, V> MultiKeyValueNode<K, V> dummy() {
//        class Dummy implements MultiKeyValueNode<K, V>, ListNode<Object> {
//            @Override
//            public ListNode<V[]> forKeys(K... keys) {
//                return (ListNode) this;
//            }
//            @Override
//            public void put(K key, V value) { }
//            @Override
//            public void put(Map<? extends K, ? extends V> map) { }
//            @Override
//            public void add(Object entry) { }
//            @Override
//            public void batch(Object... values) { }
//            @Override
//            public void batch(Collection<? extends Object> values) { }
//        }
//        return new Dummy();
//    }
}
