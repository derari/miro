package org.cthul.miro.composer;

import java.util.Arrays;

/**
 *
 */
public interface QueryComposer {
    
    default void require(Object key) {
        put2(key, null, (Object[]) null);
    }
    
    default void put(Object key, Object... args) {
        put2(key, null, args);
    }
    
    void put2(Object key, Object key2, Object... args);
    
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
