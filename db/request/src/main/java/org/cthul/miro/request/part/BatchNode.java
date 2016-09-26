package org.cthul.miro.request.part;

import java.util.Collection;

/**
 *
 * @param <T>
 */
public interface BatchNode<T> {
    
    void set(T... values);
    
    void set(Collection<? extends T> values);
}
