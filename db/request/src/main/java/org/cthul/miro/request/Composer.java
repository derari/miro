package org.cthul.miro.request;

import java.util.Arrays;
import java.util.function.Consumer;
import org.cthul.miro.util.Key;

/**
 * A builder for db statements.
 */
public interface Composer {
    
    /**
     * Ensures that the {@code key} was applied to the statement.
     * @param key 
     * @throws IllegalArgumentException if {@code key} is not recognized
     */
    default void require(Object key) {
        if (!include(key)) {
            throw new IllegalArgumentException("Unknown key: " + key);
        }
    }
    
    boolean include(Object key);
    
    /**
     * Returns the node that corresponds to {@code key}, or {@code null}
     * if key is invalid/unknown.
     * @param <V>
     * @param key
     * @return part
     * @throws IllegalArgumentException if {@code key} is not recognized
     */
    default <V> V node(Key<V> key) {
        V node = get(key);
        if (node == null) {
            require(key);
            throw new IllegalArgumentException("Parts-only key: " + key);
        }
        return node;
    }
    
    <V> V get(Key<V> key);
    
    default <V> V optional(Key<V> key, Consumer<V> action) {
        V v = get(key);
        if (v != null) action.accept(v);
        return v;
    }
            
    default void requireAll(Object... keys) {
        requireAll(Arrays.asList(keys));
    }
    
    default void requireAll(Iterable<?> keys) {
        for (Object k: keys) {
            if (k instanceof Object[]) {
                requireAll((Object[]) k);
            } else if (k instanceof Iterable) {
                requireAll((Iterable<?>) k);
            } else {
                require(k);
            }
        }
    }
}
