package org.cthul.miro.cursor;

import java.util.ConcurrentModificationException;
import java.util.Iterator;

/**
 * Iterator for the records of a query result. 
 * Unlike regular iterators, cursors may always return the same instance, 
 * and update its fields instead. Thus, the values of the cursor may never be
 * used outside of the loop.
 * <p>
 * See {@link #getFixCopy()}.
 * 
 * @param <V> 
 */
public interface ResultCursor<V> extends Iterable<V>, Iterator<V>, AutoCloseable {

    /**
     * Returns the current value. May change when {@link #next()} is called.
     * @return current value
     */
    V getValue();

    /** {@inheritDoc} */
    @Override
    boolean hasNext();

    /**
     * Returns the next element. May always return the same instance,
     * but with updated internal state.
     * @return the next element
     */
    @Override
    V next();

    /**
     * Returns a copy of the current value that will not change
     * when {@link #next()} is called.
     * @return copy of current value
     */
    V getFixCopy();

    /**
     * Returns an iterator that detects concurrent modifications and throws
     * respective {@linkplain ConcurrentModificationException exceptions}.
     * @return checked iterator
     */
    @Override
    Iterator<V> iterator();
}
