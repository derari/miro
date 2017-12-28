package org.cthul.miro.request.part;

import java.util.Collection;

/**
 *
 * @param <T>
 */
public interface BatchNode<T> {
    
    void batch(T... values);
    
    void batch(Collection<? extends T> values);
}
