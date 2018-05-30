package org.cthul.miro.composer.node;

import java.util.Collection;

/**
 *
 * @param <T>
 */
public interface BatchNode<T> {
    
    void batch(T... values);
    
    void batch(Collection<? extends T> values);
}
