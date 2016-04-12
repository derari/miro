package org.cthul.miro.result;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import org.cthul.miro.db.MiException;

/**
 *
 */
public interface ResultIterator<V> extends Iterable<V>, Iterator<V>, AutoCloseable {

    /**
     * Returns an iterator that detects concurrent modifications and throws
     * respective {@linkplain ConcurrentModificationException exceptions}.
     * @return checked iterator
     */
    @Override
    Iterator<V> iterator();

    @Override
    void close() throws MiException;
}
