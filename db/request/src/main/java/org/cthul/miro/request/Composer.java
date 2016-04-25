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
    void require(Object key);
    
    /**
     * Returns the node that corresponds to {@code key}, or {@code null}
     * if key is invalid/unknown.
     * @param <V>
     * @param key
     * @return part
     */
    <V> V node(Key<V> key);
    
    default <V> V optional(Key<V> key, Consumer<V> action) {
        V v = node(key);
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
