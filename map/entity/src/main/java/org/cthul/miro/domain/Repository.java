package org.cthul.miro.domain;

import org.cthul.miro.db.MiException;

/**
 *
 */
public interface Repository extends AutoCloseable {
    
    <E> EntitySet<E> getEntitySet(Object key);
    
    default <E> EntitySet<E> getEntitySet(Class<E> key) {
        return getEntitySet((Object) key);
    }

    @Override
    default void close() throws MiException {
    }
}
