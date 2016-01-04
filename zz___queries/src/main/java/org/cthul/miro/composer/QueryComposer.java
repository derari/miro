package org.cthul.miro.composer;

import java.util.Arrays;
import org.cthul.miro.util.Key;

/**
 *
 */
public interface QueryComposer {
    
    void require(Object key);
    
    <V> V part(Key<V> key);
            
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
